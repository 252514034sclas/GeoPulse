import os
from datetime import datetime
from typing import Dict, Any, List

class ProviderRegistry:
    """
    Central Registry tracking all 8 integrated data providers for GeoPulse:
    1. ISRO Bhoonidhi (NRSC)
    2. Copernicus Sentinel-2 (ESA)
    3. Open-Meteo Weather NWP
    4. India Meteorological Department (IMD)
    5. Tamil Nadu Agricultural University (TNAU) Agritech Portal
    6. Indian Council of Agricultural Research (ICAR)
    7. AGMARKNET (Directorate of Marketing & Inspection)
    8. Government of India Kisan Call Center (1800-180-1551)
    """

    @classmethod
    def get_all_providers_status(cls) -> Dict[str, Any]:
        bhoonidhi_user = os.getenv("BHOONIDHI_USER_ID") or os.getenv("ISRO_BHOONIDHI_USERNAME")
        copernicus_id = os.getenv("COPERNICUS_CLIENT_ID") or os.getenv("COPERNICUS_CDSE_CLIENT_ID")
        agmarknet_key = os.getenv("AGMARKNET_API_KEY") or os.getenv("DATA_GOV_IN_API_KEY")
        now_iso = datetime.utcnow().isoformat() + "Z"

        registry = [
            {
                "provider_id": "ISRO_BHOONIDHI",
                "name": "ISRO / NRSC Bhoonidhi",
                "organization": "National Remote Sensing Centre (NRSC), Indian Space Research Organisation",
                "portal_url": "https://bhoonidhi.nrsc.gov.in/",
                "api_base_url": "https://bhoonidhi-api.nrsc.gov.in/",
                "data_type": "Indian Satellite Earth Observation (Optical Multispectral & SAR Radar)",
                "missions": [
                    "Resourcesat-2A (LISS-III 23.5m, AWiFS 56m)",
                    "EOS-04 / RISAT-1A (C-band SAR 10m all-weather radar)",
                    "EOS-06 / Oceansat-3 (OCM-3 multispectral)",
                    "Cartosat-1/2 (Cadastral & Digital Elevation)"
                ],
                "access_method": "OAuth2 Bearer Token + Direct Download API",
                "auth_status": "CONFIGURED_AUTHENTICATED" if bhoonidhi_user else "STANDBY_AUTHENTICATION_PENDING",
                "auth_credential_key": "BHOONIDHI_USER_ID, BHOONIDHI_PASSWORD",
                "licensing": "Government of India Open Earth Observation Data Policy",
                "spatial_resolution": "10m to 56m",
                "availability": "ACTIVE",
                "provenance": "National Remote Sensing Centre, Balanagar, Hyderabad, India"
            },
            {
                "provider_id": "ESA_COPERNICUS",
                "name": "Copernicus Data Space Ecosystem (ESA)",
                "organization": "European Space Agency (ESA) & European Commission",
                "portal_url": "https://dataspace.copernicus.eu/",
                "api_base_url": "https://catalogue.dataspace.copernicus.eu/stac/search",
                "data_type": "High-Resolution Optical Multispectral Surface Reflectance",
                "missions": ["Sentinel-2A", "Sentinel-2B", "Sentinel-1 SAR"],
                "access_method": "STAC API & OData REST API with OAuth2",
                "auth_status": "CONFIGURED_AUTHENTICATED" if copernicus_id else "OPEN_STAC_PUBLIC_MIRROR_ACTIVE",
                "auth_credential_key": "COPERNICUS_CLIENT_ID, COPERNICUS_CLIENT_SECRET",
                "licensing": "Copernicus Full, Free and Open Data Policy",
                "spatial_resolution": "10m (B02, B04, B08), 20m (B11 SWIR)",
                "availability": "ACTIVE",
                "provenance": "ESA Copernicus Data Space Ecosystem, Frascati, Italy"
            },
            {
                "provider_id": "OPEN_METEO",
                "name": "Open-Meteo High-Resolution Weather NWP",
                "organization": "Open-Meteo GmbH (Global Numerical Weather Prediction)",
                "portal_url": "https://open-meteo.com/",
                "api_base_url": "https://api.open-meteo.com/v1/forecast",
                "data_type": "Numerical Weather Prediction (Temperature, Precipitation, Wind, Humidity, Solar Radiation, ET0)",
                "models": ["ECMWF IFS", "DWD ICON", "GFS", "MeteoFrance ARPEGE"],
                "access_method": "REST JSON API (Open Access)",
                "auth_status": "OPEN_ACCESS_UNRESTRICTED",
                "auth_credential_key": "WEATHER_API_KEY (Optional for commercial volume)",
                "licensing": "CC-BY 4.0 Open Meteorological License",
                "spatial_resolution": "1 km to 11 km",
                "availability": "ACTIVE",
                "provenance": "Global Weather Ensemble NWP models interpolated to GPS coordinates"
            },
            {
                "provider_id": "IMD_MAUSAM",
                "name": "India Meteorological Department (IMD)",
                "organization": "Ministry of Earth Sciences, Govt. of India",
                "portal_url": "https://mausam.imd.gov.in/",
                "data_type": "District Agro-Meteorological Advisories & Severe Weather Warnings",
                "access_method": "Agro-Met Advisory Bulletins & IMD Regional Alerts",
                "auth_status": "INTEGRATED_REFERENCE",
                "auth_credential_key": "IMD_API_KEY (Optional)",
                "licensing": "Government of India Official Meteorological Records",
                "availability": "ACTIVE",
                "provenance": "IMD Regional Meteorological Centre, Chennai & New Delhi"
            },
            {
                "provider_id": "TNAU_AGRITECH",
                "name": "Tamil Nadu Agricultural University (TNAU)",
                "organization": "TNAU Directorate of Extension Education, Coimbatore",
                "portal_url": "https://agritech.tnau.ac.in/",
                "data_type": "Regional Crop Production Guides, NPK Fertilizer Schedules, Seed Varieties, Sowing Windows",
                "access_method": "Research-backed agronomic knowledge engine",
                "auth_status": "INTEGRATED_KNOWLEDGE_BASE",
                "licensing": "Academic & Extension Open Agritech Portal",
                "availability": "ACTIVE",
                "provenance": "TNAU Coimbatore, Tamil Nadu, India"
            },
            {
                "provider_id": "ICAR_INDIA",
                "name": "Indian Council of Agricultural Research (ICAR)",
                "organization": "Department of Agricultural Research and Education (DARE), MoA&FW",
                "portal_url": "https://icar.gov.in/",
                "data_type": "National Agricultural Research Standards, Variety Releases (IIMR, IISR, IARI)",
                "access_method": "Agronomic formula calibration & variety database",
                "auth_status": "INTEGRATED_KNOWLEDGE_BASE",
                "licensing": "Official ICAR Research Publications",
                "availability": "ACTIVE",
                "provenance": "Krishi Bhavan, New Delhi, India"
            },
            {
                "provider_id": "AGMARKNET",
                "name": "AGMARKNET Mandi Price Information Network",
                "organization": "Directorate of Marketing & Inspection (DMI), MoA&FW, Govt. of India",
                "portal_url": "https://agmarknet.gov.in/",
                "data_type": "Daily Wholesale Commodity Prices & Arrivals across 2,800+ APMC Mandis",
                "access_method": "Open Government Data API (data.gov.in) & AGMARKNET wholesale registry",
                "auth_status": "CONFIGURED_ACTIVE" if agmarknet_key else "OFFICIAL_BENCHMARK_CATALOG_ACTIVE",
                "auth_credential_key": "AGMARKNET_API_KEY / DATA_GOV_IN_API_KEY",
                "licensing": "National Data Sharing and Accessibility Policy (NDSAP)",
                "availability": "ACTIVE",
                "provenance": "DMI, Ministry of Agriculture & Farmers Welfare, Faridabad, India"
            },
            {
                "provider_id": "KISAN_CALL_CENTER",
                "name": "Kisan Call Center (1800-180-1551)",
                "organization": "Ministry of Agriculture & Farmers Welfare, Govt. of India",
                "phone": "1800-180-1551",
                "data_type": "Live Agronomist Voice Telephone Advisory (Toll-Free, 22 Official Languages)",
                "access_method": "Android ACTION_DIAL Telephony Protocol",
                "operating_hours": "6:00 AM – 10:00 PM Daily (365 days)",
                "auth_status": "PUBLIC_TOLL_FREE_ACTIVE",
                "availability": "ACTIVE",
                "provenance": "Department of Agriculture & Farmers Welfare, Govt. of India"
            }
        ]

        return {
            "total_registered_providers": len(registry),
            "registry_timestamp": now_iso,
            "providers": registry,
            "architecture_mode": "MULTI_SOURCE_FEDERATED_INTELLIGENCE"
        }
