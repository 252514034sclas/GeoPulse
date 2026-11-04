import httpx
import xml.etree.ElementTree as ET

def check_granule_metadata():
    granule = "L2A_T44PMV_A010957_20261011T050657"
    base_blob = f"https://sentinel2l2a01.blob.core.windows.net/sentinel2-l2/44/P/MV/2026/10/11/S2C_MSIL2A_20261011T045721_N0513_R119_T44PMV_20261011T095104.SAFE/GRANULE/{granule}/MTD_TL.xml"
    
    with httpx.Client(timeout=30.0) as client:
        sign_req = f"https://planetarycomputer.microsoft.com/api/sas/v1/sign?href={base_blob}"
        r = client.get(sign_req)
        if r.status_code == 200:
            signed_url = r.json()["href"]
            xml_resp = client.get(signed_url)
            print("XML fetch status:", xml_resp.status_code)
            if xml_resp.status_code == 200:
                root = ET.fromstring(xml_resp.content)
                # Look for BOA_ADD_OFFSET or QUANTIFICATION_VALUE
                for elem in root.iter():
                    if "BOA_ADD_OFFSET" in elem.tag or "QUANTIFICATION" in elem.tag or "OFFSET" in elem.tag:
                        print(f"{elem.tag}: {elem.text} (attrib: {elem.attrib})")
        else:
            print("Failed to sign granule MTD:", r.status_code)

if __name__ == "__main__":
    check_granule_metadata()
