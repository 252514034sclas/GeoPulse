import numpy as np

def reference_unpack_15bit(data_bytes: bytes, count: int) -> np.ndarray:
    """
    Pure reference bit-by-bit unpacker adhering to TIFF 6.0 FillOrder=1 (MSB-to-LSB).
    Each byte is unpacked to 8 bits, bits are grouped into 15-bit chunks, and converted to uint16.
    """
    # Convert every byte to 8 bits (MSB first)
    bit_chunks = np.unpackbits(np.frombuffer(data_bytes, dtype=np.uint8))
    # We take exactly count * 15 bits
    total_bits = count * 15
    bits = bit_chunks[:total_bits].reshape(count, 15)
    # Convert each 15-bit row into uint16 using powers of 2
    weights = (1 << np.arange(14, -1, -1, dtype=np.uint16))
    return np.dot(bits.astype(np.uint16), weights)

def test_decoder_match():
    from process_real_satellite_raster import unpack_15bit
    
    # Generate test random 15-byte chunks
    np.random.seed(42)
    test_bytes = np.random.randint(0, 256, size=15 * 1000, dtype=np.uint8).tobytes()
    
    ref_result = reference_unpack_15bit(test_bytes, 8000)
    custom_result = unpack_15bit(test_bytes, (8000,)).ravel()
    
    match = np.array_equal(ref_result, custom_result)
    print(f"Reference unpacker vs custom unpack_15bit match: {match}")
    if not match:
        diffs = np.where(ref_result != custom_result)[0]
        print(f"Differences found at indices: {diffs[:10]}")
        print("Reference:", ref_result[diffs[:5]])
        print("Custom:   ", custom_result[diffs[:5]])
    else:
        print("PERFECT MATCH: custom unpack_15bit is mathematically identical to TIFF 6.0 MSB-to-LSB bitstream unpacking!")

if __name__ == "__main__":
    test_decoder_match()
