import httpx
import xml.etree.ElementTree as ET

def check_product_metadata():
    base_blob = "https://sentinel2l2a01.blob.core.windows.net/sentinel2-l2/44/P/MV/2026/10/11/S2C_MSIL2A_20261011T045721_N0513_R119_T44PMV_20261011T095104.SAFE/MTD_MSIL2A.xml"
    
    with httpx.Client(timeout=30.0) as client:
        sign_req = f"https://planetarycomputer.microsoft.com/api/sas/v1/sign?href={base_blob}"
        r = client.get(sign_req)
        if r.status_code == 200:
            signed_url = r.json()["href"]
            xml_resp = client.get(signed_url)
            print("Product XML fetch status:", xml_resp.status_code)
            if xml_resp.status_code == 200:
                root = ET.fromstring(xml_resp.content)
                for elem in root.iter():
                    tag = elem.tag.split("}")[-1]
                    if any(k in tag for k in ["BOA_ADD_OFFSET", "QUANTIFICATION_VALUE", "SPECIAL_VALUE"]):
                        print(f"Tag: {tag}, Text: {elem.text}, Attr: {elem.attrib}")
        else:
            print("Failed to sign product MTD:", r.status_code)

if __name__ == "__main__":
    check_product_metadata()
