import httpx

BASE = 'http://127.0.0.1:8000'

def main():
    print("=" * 70)
    print("GeoPulse Live PostgreSQL Multi-User Data Isolation Verification")
    print("=" * 70)

    with httpx.Client(base_url=BASE, timeout=10.0) as client:
        # 1. Login Account A
        res_a = client.post('/api/v1/auth/login', json={'email': 'farmer_release@geopulse.com', 'password': 'SecurePass123!'})
        assert res_a.status_code == 200, f"Login A failed: {res_a.text}"
        tok_a = res_a.json()['access_token']
        headers_a = {'Authorization': f'Bearer {tok_a}'}

        # 2. Login Account B
        res_b = client.post('/api/v1/auth/login', json={'email': 'kavitha.farmer@geopulse.com', 'password': 'KavithaPass2026!'})
        assert res_b.status_code == 200, f"Login B failed: {res_b.text}"
        tok_b = res_b.json()['access_token']
        headers_b = {'Authorization': f'Bearer {tok_b}'}

        # 3. Create parcel for Account B
        wkt_b = 'POLYGON((77.2000 11.1000, 77.2100 11.1005, 77.2090 11.1100, 77.1990 11.1095, 77.2000 11.1000))'
        resp = client.post(
            f'/api/v1/fields/draw?name=Sundaram+Cotton+Parcel+1&crop_type=Cotton&soil_type=Black+Cotton&boundary_wkt={wkt_b}',
            headers=headers_b
        )
        assert resp.status_code == 200, f"Field creation failed: {resp.text}"
        field_b = resp.json()
        field_b_id = field_b['id']
        print(f"[OK] Account B Created Field: ID={field_b_id}, Name={field_b['name']}, Area={field_b['area_acres']} acres, Soil={field_b['soil_type']}")

        # 4. Verify Account A fields
        fields_a = client.get('/api/v1/fields', headers=headers_a).json()
        field_a_ids = [f['id'] for f in fields_a]
        print(f"[OK] Account A Fields Count: {len(fields_a)}, Field IDs: {field_a_ids}")
        assert field_b_id not in field_a_ids, "DATA LEAK: Account B field leaked into Account A!"

        # 5. Verify Account B fields (must not include any Account A fields)
        fields_b = client.get('/api/v1/fields', headers=headers_b).json()
        field_b_ids = [f['id'] for f in fields_b]
        print(f"[OK] Account B Fields Count: {len(fields_b)}, Field IDs: {field_b_ids}")
        assert 26 not in field_b_ids, "DATA LEAK: Account A field 26 leaked to Account B!"
        assert field_b_id in field_b_ids, f"Created field {field_b_id} not found in Account B fields!"

        # 6. IDOR Check: Account A attempts to read Account B's field
        res_idor1 = client.get(f'/api/v1/fields/{field_b_id}', headers=headers_a)
        print(f"[OK] Anti-IDOR Check (Account A -> Field B {field_b_id}): Status={res_idor1.status_code}")
        assert res_idor1.status_code == 403, f"IDOR Vulnerability! Expected 403, got {res_idor1.status_code}"

        # 7. IDOR Check: Account B attempts to read Account A's field (Field 26)
        res_idor2 = client.get('/api/v1/fields/26', headers=headers_b)
        print(f"[OK] Anti-IDOR Check (Account B -> Field A 26): Status={res_idor2.status_code}")
        assert res_idor2.status_code == 403, f"IDOR Vulnerability! Expected 403, got {res_idor2.status_code}"

        # 8. IDOR Check: Satellite health on unowned field
        res_idor_sat = client.get('/api/v1/satellite/health/26', headers=headers_b)
        print(f"[OK] Anti-IDOR Check (Account B -> Satellite Health Field 26): Status={res_idor_sat.status_code}")
        assert res_idor_sat.status_code == 403, f"IDOR Vulnerability on satellite endpoint! Got {res_idor_sat.status_code}"

        # 9. IDOR Check: Irrigation recommendation on unowned field
        res_idor_irr = client.get('/api/v1/fields/26/irrigation', headers=headers_b)
        print(f"[OK] Anti-IDOR Check (Account B -> Irrigation Field 26): Status={res_idor_irr.status_code}")
        assert res_idor_irr.status_code == 403, f"IDOR Vulnerability on irrigation endpoint! Got {res_idor_irr.status_code}"

        # 10. IDOR Check: Sowing recommendation on unowned field
        res_idor_sow = client.get('/api/v1/fields/26/sowing-recommendation', headers=headers_b)
        print(f"[OK] Anti-IDOR Check (Account B -> Sowing Field 26): Status={res_idor_sow.status_code}")
        assert res_idor_sow.status_code == 403, f"IDOR Vulnerability on sowing endpoint! Got {res_idor_sow.status_code}"

        # 11. Delete attempt on unowned field
        res_idor_del = client.delete('/api/v1/fields/26', headers=headers_b)
        print(f"[OK] Anti-IDOR Check (Account B -> DELETE Field 26): Status={res_idor_del.status_code}")
        assert res_idor_del.status_code == 403, f"IDOR Vulnerability on delete! Got {res_idor_del.status_code}"

    print("-" * 70)
    print("ALL 11 POSTGRESQL MULTI-USER ISOLATION & ANTI-IDOR CHECKS PASSED (100% PASS)!")
    print("=" * 70)

if __name__ == '__main__':
    main()
