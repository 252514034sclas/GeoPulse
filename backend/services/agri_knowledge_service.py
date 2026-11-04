import logging
from typing import Dict, Any, List, Optional

logger = logging.getLogger("geopulse.agri_knowledge")

class AgriculturalKnowledgeService:
    """
    Expert agricultural extension service integrating official research-backed practices from:
    1. Tamil Nadu Agricultural University (TNAU) Agritech Portal (https://agritech.tnau.ac.in/)
    2. Indian Council of Agricultural Research (ICAR) (https://icar.gov.in/)
    3. Krishi Vigyan Kendra (KVK) Network (https://kvk.icar.gov.in/)
    4. Kisan Call Center (Government of India Toll-Free 1800-180-1551)

    Covers: Crop calendars, stage-specific fertilizer split dosages, irrigation water balance,
    and eco-friendly Integrated Pest Management (IPM).
    """

    KNOWLEDGE_BASE: Dict[str, Any] = {
        "corn": {
            "display_name": "Maize / Corn (Dent & Hybrid)",
            "botanical_name": "Zea mays L.",
            "recommended_varieties": ["TNAU Maize Hybrid CO 6", "COH(M) 8", "DKC 9108", "NK 6240"],
            "recommended_seed_rate_kg_ha": 20.0,
            "sowing_windows": {
                "Kharif (Adipattam)": "June 15 – July 15",
                "Rabi (Purattasipattam)": "September 15 – October 15",
                "Summer": "January 15 – February 15"
            },
            "npk_recommendation_kg_ha": {
                "total_n": 135.0,
                "total_p": 62.5,
                "total_k": 50.0,
                "split_schedule": "Basal: 25% N + 100% P + 100% K at sowing; 1st Top Dress: 50% N at knee-high stage (Day 25); 2nd Top Dress: 25% N at tasseling stage (Day 45)."
            },
            "critical_irrigation_stages": [
                {"stage": "Germination", "days": "0-5 DAS", "water_need": "Moist seedbed"},
                {"stage": "Knee-high", "days": "25-30 DAS", "water_need": "Moderate irrigation"},
                {"stage": "Tasseling & Silking", "days": "45-55 DAS", "water_need": "Critical (moisture stress causes poor pollination)"},
                {"stage": "Grain Filling / Dough", "days": "65-75 DAS", "water_need": "Adequate moisture for grain weight"}
            ],
            "common_pests": [
                {
                    "pest_name": "Fall Armyworm (Spodoptera frugiperda)",
                    "local_name": "Padai Puzhu",
                    "damage_symptoms": "Pinholes and large irregular feeding holes on whorl leaves with sawdust-like fecal matter.",
                    "ipm_management": "Erect pheromone traps @ 12/ha. Conserve natural predators (parasitoids). Spray Neem Seed Kernel Extract (NSKE 5%) or Azadirachtin 1500 ppm @ 5 ml/L at early whorl stage. For severe infestation: Emamectin benzoate 5% SG @ 0.4 g/L."
                },
                {
                    "pest_name": "Stem Borer (Chilo partellus)",
                    "local_name": "Thandu Thulaipan",
                    "damage_symptoms": "Shot holes on leaves and dead hearts in young plants.",
                    "ipm_management": "Release Trichogramma chilonis egg parasitoids @ 2.5 lakh/ha. Apply Carbofuran 3G @ 33 kg/ha in leaf whorls."
                }
            ],
            "source": "TNAU Agritech Portal — Crop Production Guide (Millets: Maize) & ICAR-IIMR"
        },
        "soybean": {
            "display_name": "Soybean",
            "botanical_name": "Glycine max (L.) Merr.",
            "recommended_varieties": ["JS 335 (ICAR-IISR)", "JS 95-60", "CO (Soy) 3", "MACS 1407"],
            "recommended_seed_rate_kg_ha": 75.0,
            "sowing_windows": {
                "Kharif": "June 20 – July 10",
                "Summer": "January 15 – February 10"
            },
            "npk_recommendation_kg_ha": {
                "total_n": 20.0,
                "total_p": 60.0,
                "total_k": 40.0,
                "split_schedule": "Legume crop fixes atmospheric nitrogen. Apply 100% N, P, K as basal dose along with Rhizobium and Phosphobacteria biofertilizers (each @ 2 kg/ha)."
            },
            "critical_irrigation_stages": [
                {"stage": "Branching", "days": "20-25 DAS", "water_need": "Moderate"},
                {"stage": "Flowering", "days": "35-45 DAS", "water_need": "Critical"},
                {"stage": "Pod Development", "days": "55-65 DAS", "water_need": "Critical (prevents pod shedding)"}
            ],
            "common_pests": [
                {
                    "pest_name": "Girdle Beetle (Oberia brevis)",
                    "local_name": "Valayal Vandu",
                    "damage_symptoms": "Girdling of petiole and stem causing withering of leaves and plant death.",
                    "ipm_management": "Collect and destroy infested plant parts during early infestation. Spray Chlorantraniliprole 18.5% SC @ 0.3 ml/L or Thiamethoxam 12.6% + Lambda-cyhalothrin 9.5% ZC @ 0.25 ml/L."
                }
            ],
            "source": "ICAR - Indian Institute of Soybean Research (IISR) & TNAU Agritech Portal"
        },
        "paddy": {
            "display_name": "Paddy / Rice",
            "botanical_name": "Oryza sativa L.",
            "recommended_varieties": ["CR 1009 Sub 1", "BPT 5204 (Samba Mahsuri)", "CO 51", "ADT 43", "ASD 16"],
            "recommended_seed_rate_kg_ha": 40.0,
            "sowing_windows": {
                "Kuruvai": "May – June",
                "Samba / Thaladi": "August – September",
                "Navarai": "December – January"
            },
            "npk_recommendation_kg_ha": {
                "total_n": 120.0,
                "total_p": 40.0,
                "total_k": 40.0,
                "split_schedule": "Basal: 25% N + 100% P + 50% K; Tillering (20 DAT): 25% N; Panicle Initiation (45 DAT): 25% N + 50% K; Heading (65 DAT): 25% N."
            },
            "critical_irrigation_stages": [
                {"stage": "Tillering", "days": "15-30 DAT", "water_need": "Shallow water depth (2-3 cm)"},
                {"stage": "Panicle Initiation", "days": "40-50 DAT", "water_need": "Crucial (keep 5 cm water)"},
                {"stage": "Flowering / Anthesis", "days": "65-75 DAT", "water_need": "Continuous submergence (5 cm)"}
            ],
            "common_pests": [
                {
                    "pest_name": "Yellow Stem Borer (Scirpophaga incertulas)",
                    "local_name": "Kuruthu Poochi",
                    "damage_symptoms": "Dead hearts during vegetative stage and white ears with empty grains at heading stage.",
                    "ipm_management": "Install light traps and pheromone traps @ 12/ha. Release Trichogramma japonicum @ 1 lakh/ha weekly. Apply Cartap hydrochloride 4G @ 25 kg/ha or Chlorantraniliprole 0.4% G @ 10 kg/ha."
                }
            ],
            "source": "TNAU Agritech Portal — Crop Production Guide (Paddy) & ICAR-NRRI"
        },
        "wheat": {
            "display_name": "Wheat",
            "botanical_name": "Triticum aestivum L.",
            "recommended_varieties": ["HD 2967", "PBW 550", "GW 322", "DBW 187"],
            "recommended_seed_rate_kg_ha": 100.0,
            "sowing_windows": {
                "Timely Sown": "November 01 – November 15",
                "Late Sown": "November 25 – December 10"
            },
            "npk_recommendation_kg_ha": {
                "total_n": 120.0,
                "total_p": 60.0,
                "total_k": 40.0,
                "split_schedule": "Basal: 50% N + 100% P + 100% K; 1st Top Dress at CRI stage (21 DAS): 25% N; 2nd Top Dress at Jointing stage (45 DAS): 25% N."
            },
            "critical_irrigation_stages": [
                {"stage": "Crown Root Initiation (CRI)", "days": "20-25 DAS", "water_need": "Most critical irrigation"},
                {"stage": "Tillering & Jointing", "days": "40-45 DAS", "water_need": "Significant water uptake"},
                {"stage": "Flowering & Milking", "days": "75-85 DAS", "water_need": "Essential for grain plumping"}
            ],
            "common_pests": [
                {
                    "pest_name": "Yellow Rust (Puccinia striiformis)",
                    "local_name": "Manjal Thirunooru",
                    "damage_symptoms": "Yellow pustules arranged in linear stripes along leaf veins.",
                    "ipm_management": "Grow resistant varieties (DBW 187 / HD 3086). Spray Propiconazole 25% EC @ 1 ml/L at first appearance of disease."
                }
            ],
            "source": "ICAR-Indian Institute of Wheat & Barley Research (IIWBR) & TNAU"
        }
    }

    @classmethod
    def get_crop_advisory(cls, crop_name: str, growth_stage: Optional[str] = None) -> Dict[str, Any]:
        """Lookup expert research advisory for crop."""
        key = "corn"
        c_lower = crop_name.lower()
        if "soy" in c_lower:
            key = "soybean"
        elif "rice" in c_lower or "paddy" in c_lower:
            key = "paddy"
        elif "wheat" in c_lower:
            key = "wheat"

        crop_info = cls.KNOWLEDGE_BASE.get(key, cls.KNOWLEDGE_BASE["corn"])
        return {
            "crop": crop_info["display_name"],
            "botanical_name": crop_info["botanical_name"],
            "growth_stage_inquired": growth_stage or "Vegetative to Flowering",
            "recommended_varieties": crop_info["recommended_varieties"],
            "seed_rate_kg_ha": crop_info["recommended_seed_rate_kg_ha"],
            "sowing_windows": crop_info["sowing_windows"],
            "fertilizer_schedule": crop_info["npk_recommendation_kg_ha"],
            "critical_watering_stages": crop_info["critical_irrigation_stages"],
            "pest_and_disease_ipm": crop_info["common_pests"],
            "government_helpline": "Kisan Call Center: 1800-180-1551 (Toll-Free, 6 AM - 10 PM, 22 languages)",
            "official_source": crop_info["source"]
        }
