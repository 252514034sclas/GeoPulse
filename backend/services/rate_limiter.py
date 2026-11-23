import time
import threading
from typing import Dict, List
from fastapi import Request, HTTPException, status

class InMemoryRateLimiter:
    """
    Thread-safe, sliding-window rate limiter for sensitive authentication and API endpoints.
    Protects login, account registration, and password recovery against brute-force attacks.
    """
    def __init__(self):
        self._lock = threading.Lock()
        self._requests: Dict[str, List[float]] = {}

    def get_client_ip(self, request: Request) -> str:
        """Extracts client IP considering standard reverse-proxy headers."""
        forwarded_for = request.headers.get("X-Forwarded-For")
        if forwarded_for:
            return forwarded_for.split(",")[0].strip()
        if request.client and request.client.host:
            return request.client.host
        return "127.0.0.1"

    def check_rate_limit(
        self,
        request: Request,
        endpoint_key: str,
        max_requests: int = 5,
        window_seconds: int = 60
    ) -> None:
        """
        Enforces maximum allowed requests within a sliding time window.
        Raises HTTP 429 Too Many Requests if the limit is exceeded.
        """
        client_ip = self.get_client_ip(request)
        tracker_key = f"{endpoint_key}:{client_ip}"
        now = time.time()
        window_start = now - window_seconds

        with self._lock:
            timestamps = self._requests.get(tracker_key, [])
            # Filter timestamps outside the sliding window
            valid_timestamps = [ts for ts in timestamps if ts > window_start]

            if len(valid_timestamps) >= max_requests:
                retry_after = int(window_seconds - (now - valid_timestamps[0])) + 1
                raise HTTPException(
                    status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                    detail=f"Too many requests on {endpoint_key}. Rate limit exceeded. Try again in {retry_after} seconds.",
                    headers={"Retry-After": str(max(1, retry_after))}
                )

            valid_timestamps.append(now)
            self._requests[tracker_key] = valid_timestamps

    def cleanup(self, max_age_seconds: int = 3600) -> None:
        """Prunes stale keys to prevent memory growth."""
        now = time.time()
        with self._lock:
            stale_keys = [k for k, v in self._requests.items() if not v or (now - v[-1]) > max_age_seconds]
            for k in stale_keys:
                del self._requests[k]

# Global singleton instance
rate_limiter = InMemoryRateLimiter()
