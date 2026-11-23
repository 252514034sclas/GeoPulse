import hashlib
import hmac
import os
import json
import base64
import time
from typing import Optional
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from sqlalchemy.orm import Session

import models
from database import get_db

SECRET_KEY = os.getenv("SECRET_KEY", "geopulse_agricultural_intelligence_secret_key_2026")
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_SECONDS = 60 * 60 * 24 * 7  # 7 days

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/api/v1/auth/login", auto_error=False)

DEFAULT_PBKDF2_ITERATIONS = 600000  # OWASP Recommended standard for PBKDF2-HMAC-SHA256

def hash_password(password: str, iterations: int = DEFAULT_PBKDF2_ITERATIONS) -> str:
    """
    Hash password securely using PBKDF2 with SHA-256, 16-byte cryptographically secure random salt,
    and OWASP-recommended 600,000 iterations.
    Format: pbkdf2:sha256:<iterations>$<salt_hex>$<key_hex>
    """
    salt = os.urandom(16)
    key = hashlib.pbkdf2_hmac("sha256", password.encode("utf-8"), salt, iterations)
    return f"pbkdf2:sha256:{iterations}${salt.hex()}${key.hex()}"

def verify_password(plain_password: str, hashed_password: str) -> bool:
    """
    Verify plain text password against stored hash with backward compatibility
    for legacy 100,000-iteration hashes and modern 600,000-iteration hashes.
    """
    try:
        if hashed_password.startswith("pbkdf2:sha256:"):
            # Format: pbkdf2:sha256:<iterations>$<salt_hex>$<key_hex>
            prefix, salt_hex, key_hex = hashed_password.split("$")
            iterations = int(prefix.split(":")[-1])
            salt = bytes.fromhex(salt_hex)
            expected_key = bytes.fromhex(key_hex)
            computed_key = hashlib.pbkdf2_hmac("sha256", plain_password.encode("utf-8"), salt, iterations)
            return hmac.compare_digest(expected_key, computed_key)
        else:
            # Legacy format: <salt_hex>$<key_hex> (100,000 iterations)
            parts = hashed_password.split("$")
            if len(parts) != 2:
                return False
            salt = bytes.fromhex(parts[0])
            expected_key = bytes.fromhex(parts[1])
            computed_key = hashlib.pbkdf2_hmac("sha256", plain_password.encode("utf-8"), salt, 100000)
            return hmac.compare_digest(expected_key, computed_key)
    except Exception:
        return False

def password_needs_rehash(hashed_password: str) -> bool:
    """Check if stored hash is using legacy iterations or format and should be upgraded."""
    if not hashed_password.startswith("pbkdf2:sha256:"):
        return True
    try:
        prefix = hashed_password.split("$")[0]
        iterations = int(prefix.split(":")[-1])
        return iterations < DEFAULT_PBKDF2_ITERATIONS
    except Exception:
        return True

def base64url_encode(data: bytes) -> str:
    return base64.urlsafe_b64encode(data).rstrip(b'=').decode('utf-8')

def base64url_decode(data: str) -> bytes:
    padding = '=' * (4 - (len(data) % 4)) if (len(data) % 4) != 0 else ''
    return base64.urlsafe_b64decode((data + padding).encode('utf-8'))

def create_access_token(data: dict, expires_delta_seconds: Optional[int] = None) -> str:
    """Create a standard signed JWT token using HMAC-SHA256."""
    header = {"alg": ALGORITHM, "typ": "JWT"}
    to_encode = data.copy()
    exp = int(time.time()) + (expires_delta_seconds or ACCESS_TOKEN_EXPIRE_SECONDS)
    to_encode.update({"exp": exp})

    header_b64 = base64url_encode(json.dumps(header).encode('utf-8'))
    payload_b64 = base64url_encode(json.dumps(to_encode).encode('utf-8'))
    signing_input = f"{header_b64}.{payload_b64}".encode('utf-8')
    signature = hmac.new(SECRET_KEY.encode('utf-8'), signing_input, hashlib.sha256).digest()
    sig_b64 = base64url_encode(signature)

    return f"{header_b64}.{payload_b64}.{sig_b64}"

def decode_access_token(token: str) -> Optional[dict]:
    """Decode and verify a signed JWT token."""
    try:
        parts = token.split(".")
        if len(parts) != 3:
            return None
        header_b64, payload_b64, sig_b64 = parts
        signing_input = f"{header_b64}.{payload_b64}".encode('utf-8')
        expected_sig = hmac.new(SECRET_KEY.encode('utf-8'), signing_input, hashlib.sha256).digest()
        provided_sig = base64url_decode(sig_b64)

        if not hmac.compare_digest(expected_sig, provided_sig):
            return None

        payload = json.loads(base64url_decode(payload_b64).decode('utf-8'))
        if payload.get("exp", 0) < int(time.time()):
            return None  # Token expired

        return payload
    except Exception:
        return None

def get_current_user_optional(token: Optional[str] = Depends(oauth2_scheme), db: Session = Depends(get_db)) -> Optional[models.UserModel]:
    """Get authenticated user if valid Bearer token present, otherwise None."""
    if token:
        payload = decode_access_token(token)
        if payload and "sub" in payload:
            user = db.query(models.UserModel).filter(models.UserModel.email == payload["sub"]).first()
            if user:
                return user
    return None

def get_current_user_strict(token: str = Depends(oauth2_scheme), db: Session = Depends(get_db)) -> models.UserModel:
    """Strict authentication dependency requiring valid Bearer token."""
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    if not token:
        raise credentials_exception
    payload = decode_access_token(token)
    if not payload or "sub" not in payload:
        raise credentials_exception
    user = db.query(models.UserModel).filter(models.UserModel.email == payload["sub"]).first()
    if user is None:
        raise credentials_exception
    return user
