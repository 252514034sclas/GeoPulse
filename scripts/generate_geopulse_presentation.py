import sys
import os
import pptx
from pptx import Presentation
from pptx.util import Inches, Pt
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.enum.shapes import MSO_SHAPE

def build_presentation(output_path="GeoPulse_Project_Presentation.pptx"):
    prs = Presentation()
    prs.slide_width = Inches(13.333)
    prs.slide_height = Inches(7.5)
    blank_layout = prs.slide_layouts[6]

    # Color Palette: Agritech Modern Dark & Light
    DARK_BG = RGBColor(14, 28, 18)        # #0E1C12 Deep Forest Night
    DARK_CARD = RGBColor(24, 46, 31)      # #182E1F Forest Card
    ACCENT_GREEN = RGBColor(46, 125, 50)  # #2E7D32 Deep Emerald
    BRIGHT_GREEN = RGBColor(76, 175, 80)  # #4CAF50 Vivid Leaf Green
    LIGHT_BG = RGBColor(248, 250, 248)    # #F8FAF8 Off-white clean
    WHITE = RGBColor(255, 255, 255)
    DARK_TEXT = RGBColor(28, 38, 30)      # #1C261E Charcoal Dark
    MUTED_TEXT = RGBColor(100, 116, 104)  # #647468 Slate Muted
    BORDER_COLOR = RGBColor(210, 225, 215)
    ACCENT_GOLD = RGBColor(230, 149, 0)
    CARD_BG = RGBColor(255, 255, 255)

    def set_slide_background(slide, color):
        bg = slide.background
        fill = bg.fill
        fill.solid()
        fill.fore_color.rgb = color

    def add_header(slide, title_text, category_text="GEOPULSE APP • ANDROID", dark=False):
        # Category pill/badge
        tx_box = slide.shapes.add_textbox(Inches(0.8), Inches(0.4), Inches(11.7), Inches(0.4))
        tf = tx_box.text_frame
        tf.word_wrap = True
        tf.margin_left = tf.margin_top = tf.margin_right = tf.margin_bottom = 0
        p_cat = tf.paragraphs[0]
        p_cat.text = category_text.upper()
        p_cat.font.size = Pt(10)
        p_cat.font.bold = True
        p_cat.font.color.rgb = BRIGHT_GREEN if dark else ACCENT_GREEN

        # Title
        p_title = tf.add_paragraph()
        p_title.text = title_text
        p_title.font.size = Pt(22)
        p_title.font.bold = True
        p_title.font.color.rgb = WHITE if dark else DARK_TEXT

    def add_card(slide, left, top, width, height, bg_color=CARD_BG, border_color=BORDER_COLOR):
        shape = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, top, width, height)
        shape.fill.solid()
        shape.fill.fore_color.rgb = bg_color
        if border_color:
            shape.line.color.rgb = border_color
            shape.line.width = Pt(1.5)
        else:
            shape.line.fill.background()
        return shape

    # =========================================================================
    # SLIDE 1: TITLE SLIDE (Matches user's exact details: NITHISH T, 252514034, BSC. AI & DS)
    # =========================================================================
    slide1 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide1, DARK_BG)

    # Accent decorative bar
    bar = slide1.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.8), Inches(1.2), Inches(0.3), Inches(4.8))
    bar.fill.solid()
    bar.fill.fore_color.rgb = BRIGHT_GREEN
    bar.line.fill.background()

    # Title & Subtitle text box
    tb = slide1.shapes.add_textbox(Inches(1.4), Inches(1.1), Inches(11.0), Inches(3.2))
    tf = tb.text_frame
    tf.word_wrap = True
    
    p0 = tf.paragraphs[0]
    p0.text = "ANDROID PRECISION AGRONOMY & GIS"
    p0.font.size = Pt(13)
    p0.font.bold = True
    p0.font.color.rgb = BRIGHT_GREEN
    p0.space_after = Pt(12)

    p1 = tf.add_paragraph()
    p1.text = "GEOPULSE APP: ANDROID"
    p1.font.size = Pt(36)
    p1.font.bold = True
    p1.font.color.rgb = WHITE
    p1.space_after = Pt(10)

    p2 = tf.add_paragraph()
    p2.text = "Offline-First Satellite Crop Intelligence, FAO-56 Daily Water Balance & GIS Platform"
    p2.font.size = Pt(16)
    p2.font.color.rgb = RGBColor(200, 220, 205)

    # Student Details Card
    card1 = add_card(slide1, Inches(1.4), Inches(4.3), Inches(10.5), Inches(2.2), bg_color=DARK_CARD, border_color=BRIGHT_GREEN)
    tb_author = slide1.shapes.add_textbox(Inches(1.8), Inches(4.5), Inches(9.8), Inches(1.8))
    tf_author = tb_author.text_frame
    tf_author.word_wrap = True

    pa1 = tf_author.paragraphs[0]
    pa1.text = "PRESENTED BY:"
    pa1.font.size = Pt(11)
    pa1.font.bold = True
    pa1.font.color.rgb = BRIGHT_GREEN
    pa1.space_after = Pt(6)

    pa2 = tf_author.add_paragraph()
    pa2.text = "NAME : NITHISH T"
    pa2.font.size = Pt(18)
    pa2.font.bold = True
    pa2.font.color.rgb = WHITE
    pa2.space_after = Pt(4)

    pa3 = tf_author.add_paragraph()
    pa3.text = "REG NO:  252514034\nDEP: BSC. (HONS) AI & DS"
    pa3.font.size = Pt(14)
    pa3.font.color.rgb = RGBColor(220, 235, 225)

    # =========================================================================
    # SLIDE 2: PRODUCT PROBLEM
    # =========================================================================
    slide2 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide2, LIGHT_BG)
    add_header(slide2, "PRODUCT PROBLEM: THE CRISIS IN MODERN CROP ADVISORY")

    # Overview text
    tb_intro = slide2.shapes.add_textbox(Inches(0.8), Inches(1.3), Inches(11.7), Inches(0.9))
    tf_intro = tb_intro.text_frame
    tf_intro.word_wrap = True
    pi = tf_intro.paragraphs[0]
    pi.text = "The main problem with most agricultural advisory apps today is their heavy dependence on third-party platforms or continuous internet connectivity, which limits authenticity and accessibility. This creates barriers for farmers seeking genuine precision-farming experiences, leading to:"
    pi.font.size = Pt(13)
    pi.font.color.rgb = DARK_TEXT

    # 4 Cards for the 4 core problems
    problems = [
        ("Lack of Authenticity", "Outdated or inaccurate crop schedules fail to provide a true scientific connection between satellite observations and on-field conditions.", ACCENT_GOLD),
        ("Internet Dependency", "Apps that require full-time connectivity are unusable offline, restricting flexibility in remote agricultural fields.", RGBColor(211, 47, 47)),
        ("Poor Personalization", "Many apps lack simple farm-specific customization and daily soil-water modeling, making them less user-friendly.", ACCENT_GREEN),
        ("Privacy Concerns", "Online data storage increases risks for farmers who value secure and private land ownership and crop yield information.", DARK_TEXT)
    ]

    card_w = Inches(5.6)
    card_h = Inches(2.2)
    coords = [
        (Inches(0.8), Inches(2.3)),
        (Inches(6.8), Inches(2.3)),
        (Inches(0.8), Inches(4.7)),
        (Inches(6.8), Inches(4.7))
    ]

    for idx, (title, desc, color) in enumerate(problems):
        x, y = coords[idx]
        add_card(slide2, x, y, card_w, card_h, bg_color=WHITE, border_color=BORDER_COLOR)
        
        # Indicator bar inside card
        ibar = slide2.shapes.add_shape(MSO_SHAPE.RECTANGLE, x, y, Inches(0.15), card_h)
        ibar.fill.solid()
        ibar.fill.fore_color.rgb = color
        ibar.line.fill.background()

        ctb = slide2.shapes.add_textbox(x + Inches(0.35), y + Inches(0.2), card_w - Inches(0.5), card_h - Inches(0.4))
        ctf = ctb.text_frame
        ctf.word_wrap = True
        pt = ctf.paragraphs[0]
        pt.text = title
        pt.font.size = Pt(14)
        pt.font.bold = True
        pt.font.color.rgb = DARK_TEXT
        pt.space_after = Pt(6)

        pd = ctf.add_paragraph()
        pd.text = desc
        pd.font.size = Pt(11)
        pd.font.color.rgb = MUTED_TEXT

    # =========================================================================
    # SLIDE 3: SOLUTION
    # =========================================================================
    slide3 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide3, LIGHT_BG)
    add_header(slide3, "THE SOLUTION: GEOPULSE PRECISION AGRONOMY SYSTEM")

    tb_s_intro = slide3.shapes.add_textbox(Inches(0.8), Inches(1.3), Inches(11.7), Inches(0.8))
    tf_s_intro = tb_s_intro.text_frame
    tf_s_intro.word_wrap = True
    psi = tf_s_intro.paragraphs[0]
    psi.text = "The solution is GeoPulse — an offline-first agronomic advisory app powered by multi-source satellite remote sensing (ISRO Bhoonidhi + Copernicus Sentinel-2) and the scientific FAO-56 daily water balance model that provides authentic, transparent, and actionable farm guidance anytime."
    psi.font.size = Pt(13)
    psi.font.color.rgb = DARK_TEXT

    solutions = [
        ("Offline-First Advisory Access", "Provides fully cached field water balances, irrigation schedules, and agronomic guidelines that function smoothly in zero-connectivity fields with automatic background sync.", BRIGHT_GREEN),
        ("Live Multi-Source Satellite Telemetry", "Directly queries Copernicus Sentinel-2 L2A and ISRO Bhoonidhi STAC APIs to compute real-time NDVI and NDWI vegetation health indices without dummy data.", ACCENT_GREEN),
        ("Scientific FAO-56 Water Balance", "Calculates daily reference evapotranspiration (ETo), crop coefficients (Kc), and USDA-SCS effective precipitation to deliver exact millimeter and minute pump dosages.", ACCENT_GOLD),
        ("Bilingual & Privacy by Design", "Offers full bilingual support (Tamil & English) with local SQLite on-device storage, ensuring farm boundaries and operational decisions stay secure with the cultivator.", DARK_TEXT)
    ]

    for idx, (title, desc, color) in enumerate(solutions):
        x, y = coords[idx]
        add_card(slide3, x, y, card_w, card_h, bg_color=WHITE, border_color=BORDER_COLOR)
        
        ibar = slide3.shapes.add_shape(MSO_SHAPE.RECTANGLE, x, y, Inches(0.15), card_h)
        ibar.fill.solid()
        ibar.fill.fore_color.rgb = color
        ibar.line.fill.background()

        ctb = slide3.shapes.add_textbox(x + Inches(0.35), y + Inches(0.2), card_w - Inches(0.5), card_h - Inches(0.4))
        ctf = ctb.text_frame
        ctf.word_wrap = True
        pt = ctf.paragraphs[0]
        pt.text = title
        pt.font.size = Pt(14)
        pt.font.bold = True
        pt.font.color.rgb = DARK_TEXT
        pt.space_after = Pt(6)

        pd = ctf.add_paragraph()
        pd.text = desc
        pd.font.size = Pt(11)
        pd.font.color.rgb = MUTED_TEXT

    # =========================================================================
    # SLIDE 4: PROJECT OVERVIEW (Problem vs Solution)
    # =========================================================================
    slide4 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide4, LIGHT_BG)
    add_header(slide4, "PROJECT OVERVIEW: THE PROBLEM VS. THE SOLUTION")

    # Column Left: The Problem
    add_card(slide4, Inches(0.8), Inches(1.4), Inches(5.6), Inches(5.4), bg_color=WHITE, border_color=RGBColor(230, 200, 200))
    # Red header bar
    rbar = slide4.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(0.8), Inches(1.4), Inches(5.6), Inches(0.7))
    rbar.fill.solid()
    rbar.fill.fore_color.rgb = RGBColor(198, 40, 40)
    rbar.line.fill.background()
    tb_rh = slide4.shapes.add_textbox(Inches(1.0), Inches(1.5), Inches(5.2), Inches(0.5))
    tb_rh.text_frame.paragraphs[0].text = "THE PROBLEM"
    tb_rh.text_frame.paragraphs[0].font.size = Pt(14)
    tb_rh.text_frame.paragraphs[0].font.bold = True
    tb_rh.text_frame.paragraphs[0].font.color.rgb = WHITE

    tb_prob = slide4.shapes.add_textbox(Inches(1.1), Inches(2.3), Inches(5.0), Inches(4.3))
    tf_prob = tb_prob.text_frame
    tf_prob.word_wrap = True
    prob_bullets = [
        "Rural Network Fragility: Most precision agritech platforms require continuous cloud connections, rendering them inoperative in remote agricultural fields.",
        "Uncalibrated Crop Guidance: Generic mobile tools use basic rules or static dates, leading to inaccurate advice that wastes irrigation water and fertilizers.",
        "Prohibitive Commercial Paywalls: High-end satellite analytics and GIS software cost hundreds of dollars, completely out of reach for smallholders and FPOs.",
        "Data Exploitation & Jargon: Farmers face confusing GIS jargon and privacy risks as central servers sell plot records to aggressive agro-chemical advertisers."
    ]
    for i, b in enumerate(prob_bullets):
        p = tf_prob.paragraphs[0] if i == 0 else tf_prob.add_paragraph()
        p.text = "• " + b
        p.font.size = Pt(11)
        p.font.color.rgb = DARK_TEXT
        p.space_after = Pt(10)

    # Column Right: The Solution
    add_card(slide4, Inches(6.8), Inches(1.4), Inches(5.6), Inches(5.4), bg_color=WHITE, border_color=BORDER_COLOR)
    # Green header bar
    gbar = slide4.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(6.8), Inches(1.4), Inches(5.6), Inches(0.7))
    gbar.fill.solid()
    gbar.fill.fore_color.rgb = ACCENT_GREEN
    gbar.line.fill.background()
    tb_gh = slide4.shapes.add_textbox(Inches(7.0), Inches(1.5), Inches(5.2), Inches(0.5))
    tb_gh.text_frame.paragraphs[0].text = "THE GEOPULSE SOLUTION"
    tb_gh.text_frame.paragraphs[0].font.size = Pt(14)
    tb_gh.text_frame.paragraphs[0].font.bold = True
    tb_gh.text_frame.paragraphs[0].font.color.rgb = WHITE

    tb_sol = slide4.shapes.add_textbox(Inches(7.1), Inches(2.3), Inches(5.0), Inches(4.3))
    tf_sol = tb_sol.text_frame
    tf_sol.word_wrap = True
    sol_bullets = [
        "Offline-First Resilience: GeoPulse allows farmers to monitor fields, review water balance, and check guides offline with automated SQLite cache sync.",
        "Authentic STAC Satellite Telemetry: Directly integrates ISRO Bhoonidhi and Copernicus Sentinel-2 L2A rasters for genuine NDVI/NDWI crop health analytics.",
        "Mathematical FAO-56 Precision: Integrates Penman-Monteith daily water balance and USDA-SCS effective rain to provide exact pump dosage in minutes.",
        "Bilingual Action Cards & Privacy: Offers Tamil and English interfaces, on-device encrypted storage, and real-time AGMARKNET mandi rates."
    ]
    for i, b in enumerate(sol_bullets):
        p = tf_sol.paragraphs[0] if i == 0 else tf_sol.add_paragraph()
        p.text = "✔ " + b
        p.font.size = Pt(11)
        p.font.color.rgb = DARK_TEXT
        p.space_after = Pt(10)

    # =========================================================================
    # SLIDE 5: PRODUCT DOMAIN STUDY - SUMMARY
    # =========================================================================
    slide5 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide5, LIGHT_BG)
    add_header(slide5, "PRODUCT DOMAIN STUDY: EXECUTIVE SUMMARY")

    add_card(slide5, Inches(0.8), Inches(1.4), Inches(11.7), Inches(5.4), bg_color=WHITE, border_color=BORDER_COLOR)

    tb_sum = slide5.shapes.add_textbox(Inches(1.2), Inches(1.7), Inches(11.0), Inches(4.8))
    tf_sum = tb_sum.text_frame
    tf_sum.word_wrap = True

    p_proj = tf_sum.paragraphs[0]
    p_proj.text = "DOMAIN: AI, SATELLITE REMOTE SENSING & PRECISION AGRONOMY"
    p_proj.font.size = Pt(11)
    p_proj.font.bold = True
    p_proj.font.color.rgb = ACCENT_GREEN
    p_proj.space_after = Pt(8)

    p_p1 = tf_sum.add_paragraph()
    p_p1.text = "GeoPulse is a next-generation geospatial agricultural intelligence application engineered to bridge the digital divide between high-altitude satellite remote sensing and ground-level farming operations. While traditional agricultural services rely on third-party marketing platforms or require continuous high-speed internet, GeoPulse is designed around an offline-first architecture tailored for rural agricultural belts."
    p_p1.font.size = Pt(12)
    p_p1.font.color.rgb = DARK_TEXT
    p_p1.space_after = Pt(10)

    p_p2 = tf_sum.add_paragraph()
    p_p2.text = "The application synthesizes live, open-access multispectral imagery from the European Space Agency's Copernicus Sentinel-2 constellation and ISRO's Bhoonidhi STAC catalog, extracting real Normalized Difference Vegetation Index (NDVI) and Normalized Difference Water Index (NDWI) indices at 10-meter spatial resolution. These space-borne signals are combined with hyper-local agro-meteorological feeds and the internationally standardized FAO-56 Penman-Monteith daily water balance model."
    p_p2.font.size = Pt(12)
    p_p2.font.color.rgb = DARK_TEXT
    p_p2.space_after = Pt(10)

    p_p3 = tf_sum.add_paragraph()
    p_p3.text = "By delivering bilingual (Tamil & English) action cards, real-time AGMARKNET mandi commodity rate comparisons, and an intuitive native Android interface built with Jetpack Compose, GeoPulse transforms abstract geospatial telemetry into practical daily pump durations, pest advisories, and harvest timelines that optimize farm productivity and protect natural water resources."
    p_p3.font.size = Pt(12)
    p_p3.font.color.rgb = DARK_TEXT

    # =========================================================================
    # SLIDE 6: PRODUCT DOMAIN STUDY - LINKEDIN ARTICLE & CONCLUSION
    # =========================================================================
    slide6 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide6, LIGHT_BG)
    add_header(slide6, "PRODUCT DOMAIN STUDY: ARTICLE & CONCLUSION")

    # Left: Article
    add_card(slide6, Inches(0.8), Inches(1.4), Inches(5.6), Inches(5.4), bg_color=WHITE, border_color=BORDER_COLOR)
    tb_art = slide6.shapes.add_textbox(Inches(1.1), Inches(1.6), Inches(5.0), Inches(5.0))
    tf_art = tb_art.text_frame
    tf_art.word_wrap = True

    pa_h = tf_art.paragraphs[0]
    pa_h.text = "LINKEDIN PUBLICATION SUMMARY"
    pa_h.font.size = Pt(13)
    pa_h.font.bold = True
    pa_h.font.color.rgb = ACCENT_GREEN
    pa_h.space_after = Pt(8)

    pa_c1 = tf_art.add_paragraph()
    pa_c1.text = "Democratizing Space Technology for Smallholder Farmers: How GeoPulse Fuses Satellite Telemetry with On-Field Decision Making"
    pa_c1.font.size = Pt(11)
    pa_c1.font.bold = True
    pa_c1.font.color.rgb = DARK_TEXT
    pa_c1.space_after = Pt(8)

    pa_c2 = tf_art.add_paragraph()
    pa_c2.text = "In an era of accelerating climate volatility and groundwater depletion, Indian agriculture requires accessible, data-driven solutions. GeoPulse leverages modern AI and remote sensing pipelines to transform raw satellite spectral bands into actionable daily farming guidance. By rejecting synthetic dummy data in favor of verifiable STAC satellite queries and mathematically grounded FAO-56 soil water balance algorithms, GeoPulse proves that cutting-edge space technology can operate on budget Android smartphones in low-connectivity rural environments."
    pa_c2.font.size = Pt(10)
    pa_c2.font.color.rgb = MUTED_TEXT

    # Right: Conclusion
    add_card(slide6, Inches(6.8), Inches(1.4), Inches(5.6), Inches(5.4), bg_color=WHITE, border_color=BORDER_COLOR)
    tb_con = slide6.shapes.add_textbox(Inches(7.1), Inches(1.6), Inches(5.0), Inches(5.0))
    tf_con = tb_con.text_frame
    tf_con.word_wrap = True

    pc_h = tf_con.paragraphs[0]
    pc_h.text = "DOMAIN STUDY CONCLUSION"
    pc_h.font.size = Pt(13)
    pc_h.font.bold = True
    pc_h.font.color.rgb = BRIGHT_GREEN
    pc_h.space_after = Pt(8)

    pc_c1 = tf_con.add_paragraph()
    pc_c1.text = "GeoPulse stands out as a scientifically rigorous, farmer-first platform that directly tackles the four pillars of modern agronomic vulnerability: water wastage, network instability, data opacity, and market exploitation."
    pc_c1.font.size = Pt(11)
    pc_c1.font.color.rgb = DARK_TEXT
    pc_c1.space_after = Pt(10)

    pc_c2 = tf_con.add_paragraph()
    pc_c2.text = "By uniting open satellite remote sensing (ISRO + Copernicus), offline-first Room database synchronization, bilingual Tamil/English action cards, and real-time AGMARKNET mandi price feeds, GeoPulse creates a sustainable digital ecosystem. It empowers cultivators with genuine autonomy, elevates crop yields, and ensures environmental stewardship through precision resource conservation."
    pc_c2.font.size = Pt(10)
    pc_c2.font.color.rgb = MUTED_TEXT

    # =========================================================================
    # SLIDE 7: PROBLEM STATEMENT
    # =========================================================================
    slide7 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide7, LIGHT_BG)
    add_header(slide7, "FORMAL PROBLEM STATEMENT")

    tb_ps_box = slide7.shapes.add_textbox(Inches(0.8), Inches(1.4), Inches(11.7), Inches(5.4))
    tf_ps = tb_ps_box.text_frame
    tf_ps.word_wrap = True

    points = [
        ("Groundwater Depletion & Irrigation Guesswork", "Over 80% of freshwater in India is consumed by agriculture, with farmers relying on flood irrigation and guesswork that depletes water tables, leaches soil nutrients, and increases energy costs."),
        ("Prohibitive Commercial Agritech Barriers", "Existing precision agritech platforms are tailored for corporate mega-farms, requiring costly enterprise subscriptions, expensive IoT sensor hardware, and continuous broadband that smallholders cannot afford."),
        ("Rural Network Dead Zones", "Most mobile applications freeze or fail when disconnected from cloud servers, leaving farmers without vital weather, pest, or irrigation advice right when they are standing in their fields."),
        ("Technical Jargon & Market Information Asymmetry", "Farmers are overwhelmed by complex GIS indices and disconnected from true market realizations, making them vulnerable to exploitative middlemen and volatile APMC mandi pricing."),
        ("The Core Engineering Need", "There is an imperative demand for a lightweight, native Android agritech solution that provides verified satellite telemetry, calculates calibrated FAO-56 daily water deficits, supports bilingual Tamil/English communication, and guarantees 100% offline field functionality.")
    ]

    for i, (title, desc) in enumerate(points):
        p_t = tf_ps.paragraphs[0] if i == 0 else tf_ps.add_paragraph()
        p_t.text = f"{i+1}. {title}: "
        p_t.font.size = Pt(12)
        p_t.font.bold = True
        p_t.font.color.rgb = ACCENT_GREEN

        p_d = tf_ps.add_paragraph()
        p_d.text = desc
        p_d.font.size = Pt(11)
        p_d.font.color.rgb = DARK_TEXT
        p_d.space_after = Pt(8)

    # =========================================================================
    # SLIDE 8: PROJECT SCOPE (MY ROLE & MY RESPONSIBILITIES)
    # =========================================================================
    slide8 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide8, LIGHT_BG)
    add_header(slide8, "PROJECT SCOPE: LEAD ROLE & RESPONSIBILITIES")

    # Left: My Role
    add_card(slide8, Inches(0.8), Inches(1.4), Inches(5.6), Inches(5.4), bg_color=WHITE, border_color=BORDER_COLOR)
    tb_mr = slide8.shapes.add_textbox(Inches(1.1), Inches(1.6), Inches(5.0), Inches(5.0))
    tf_mr = tb_mr.text_frame
    tf_mr.word_wrap = True

    pm_h = tf_mr.paragraphs[0]
    pm_h.text = "MY ROLE: FULL-STACK AI & ANDROID DEVELOPER"
    pm_h.font.size = Pt(13)
    pm_h.font.bold = True
    pm_h.font.color.rgb = ACCENT_GREEN
    pm_h.space_after = Pt(8)

    pm_desc = tf_mr.add_paragraph()
    pm_desc.text = "The goal was to architect, engineer, and deploy an end-to-end mobile precision agriculture platform tailored for smallholder farmers and agricultural cooperatives. My role spanned from designing the native Android client with 30+ interactive activities to developing the Python/FastAPI geospatial backend, integrating satellite STAC query engines, and formulating the scientific FAO-56 Penman-Monteith daily water balance algorithms."
    pm_desc.font.size = Pt(11)
    pm_desc.font.color.rgb = DARK_TEXT

    # Right: My Responsibilities
    add_card(slide8, Inches(6.8), Inches(1.4), Inches(5.6), Inches(5.4), bg_color=WHITE, border_color=BORDER_COLOR)
    tb_resp = slide8.shapes.add_textbox(Inches(7.1), Inches(1.6), Inches(5.0), Inches(5.0))
    tf_resp = tb_resp.text_frame
    tf_resp.word_wrap = True

    pr_h = tf_resp.paragraphs[0]
    pr_h.text = "MY RESPONSIBILITIES"
    pr_h.font.size = Pt(13)
    pr_h.font.bold = True
    pr_h.font.color.rgb = BRIGHT_GREEN
    pr_h.space_after = Pt(8)

    resps = [
        "Designing and developing 30+ native Jetpack Compose activities with a cohesive Material 3 design system.",
        "Integrating MapLibre Native Android GIS SDK with interactive farm polygon drawing and GPS geofencing.",
        "Building asynchronous FastAPI backend microservices for ISRO Bhoonidhi and Copernicus Sentinel-2 STAC queries.",
        "Implementing the scientific FAO-56 Penman-Monteith water balance and USDA-SCS effective precipitation model.",
        "Developing real-time AGMARKNET mandi commodity rate scrapers and bilingual (Tamil/English) localization.",
        "Conducting rigorous offline caching verification (Room/SQLite), UI audits, and independent agronomic validation."
    ]
    for r in resps:
        p = tf_resp.add_paragraph()
        p.text = "• " + r
        p.font.size = Pt(10)
        p.font.color.rgb = DARK_TEXT
        p.space_after = Pt(6)

    # =========================================================================
    # SLIDE 9: PROJECT PLAN (Gantt Chart Table)
    # =========================================================================
    slide9 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide9, LIGHT_BG)
    add_header(slide9, "PROJECT PLAN: 6-WEEK DEVELOPMENT SCHEDULE")

    # Table of Gantt schedule
    table_shape = slide9.shapes.add_table(7, 7, Inches(0.8), Inches(1.6), Inches(11.7), Inches(5.0))
    table = table_shape.table
    table.columns[0].width = Inches(3.3)
    for c in range(1, 7):
        table.columns[c].width = Inches(1.4)

    headers = ["Project Phase", "Week 1", "Week 2", "Week 3", "Week 4", "Week 5", "Week 6"]
    for i, h in enumerate(headers):
        cell = table.cell(0, i)
        cell.fill.solid()
        cell.fill.fore_color.rgb = DARK_BG
        cell.text_frame.paragraphs[0].text = h
        cell.text_frame.paragraphs[0].font.size = Pt(11)
        cell.text_frame.paragraphs[0].font.bold = True
        cell.text_frame.paragraphs[0].font.color.rgb = WHITE
        cell.text_frame.paragraphs[0].alignment = PP_ALIGN.CENTER

    phases = [
        ("Concept Research & Agronomy Formulation", 1),
        ("Architecture, GIS & UI Prototyping", 2),
        ("Android Native Frontend (30+ Compose Screens)", 3),
        ("Backend Services, STAC Telemetry & FAO-56 Engine", 4),
        ("Offline Cache Auditing & Field Validation", 5),
        ("Final Deployment, Acceptance & Launch", 6),
    ]

    for row_idx, (phase_name, active_week) in enumerate(phases, start=1):
        cell_p = table.cell(row_idx, 0)
        cell_p.fill.solid()
        cell_p.fill.fore_color.rgb = WHITE
        cell_p.text_frame.paragraphs[0].text = phase_name
        cell_p.text_frame.paragraphs[0].font.size = Pt(10)
        cell_p.text_frame.paragraphs[0].font.bold = True
        cell_p.text_frame.paragraphs[0].font.color.rgb = DARK_TEXT

        for w in range(1, 7):
            cell_w = table.cell(row_idx, w)
            cell_w.fill.solid()
            if w == active_week:
                cell_w.fill.fore_color.rgb = ACCENT_GREEN
                cell_w.text_frame.paragraphs[0].text = "Active"
                cell_w.text_frame.paragraphs[0].font.bold = True
                cell_w.text_frame.paragraphs[0].font.color.rgb = WHITE
            elif w < active_week:
                cell_w.fill.fore_color.rgb = RGBColor(220, 235, 225)
                cell_w.text_frame.paragraphs[0].text = "Done"
                cell_w.text_frame.paragraphs[0].font.color.rgb = MUTED_TEXT
            else:
                cell_w.fill.fore_color.rgb = RGBColor(245, 245, 245)
                cell_w.text_frame.paragraphs[0].text = "-"
                cell_w.text_frame.paragraphs[0].font.color.rgb = RGBColor(180, 180, 180)
            cell_w.text_frame.paragraphs[0].font.size = Pt(9)
            cell_w.text_frame.paragraphs[0].alignment = PP_ALIGN.CENTER

    # =========================================================================
    # SLIDE 10: PROJECT TIMELINE (Milestone Roadmap)
    # =========================================================================
    slide10 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide10, LIGHT_BG)
    add_header(slide10, "PROJECT TIMELINE & MILESTONE ROADMAP")

    # Center horizontal line
    line = slide10.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.8), Inches(3.6), Inches(11.7), Inches(0.08))
    line.fill.solid()
    line.fill.fore_color.rgb = ACCENT_GREEN
    line.line.fill.background()

    milestones = [
        ("May 1", "Project Start & Ideation", "Research into FAO-56 irrigation and Sentinel-2 STAC datasets.", False),
        ("May 28", "UI/UX & Architecture", "30+ wireframes, Jetpack Compose theme system, MapLibre prototyping.", True),
        ("June 14", "Android Native Client", "Activity modularization, explicit intent routing, custom components.", False),
        ("June 30", "Backend & Geospatial GIS", "FastAPI microservices, ISRO Bhoonidhi integration, NDVI/NDWI pipeline.", True),
        ("July 21", "Scientific Validation", "FAO-56 water balance calibration, USDA-SCS effective rain model testing.", False),
        ("August 30", "Production Deployment", "Offline Room database verification, bilingual audit, acceptance launch.", True)
    ]

    step_w = Inches(1.85)
    start_x = Inches(0.9)

    for i, (m_date, m_title, m_desc, is_top) in enumerate(milestones):
        x = start_x + (i * step_w)
        
        # Milestone Circle Node
        circle = slide10.shapes.add_shape(MSO_SHAPE.OVAL, x + Inches(0.65), Inches(3.45), Inches(0.38), Inches(0.38))
        circle.fill.solid()
        circle.fill.fore_color.rgb = WHITE
        circle.line.color.rgb = ACCENT_GREEN
        circle.line.width = Pt(3)

        # Date Badge
        dbadge = slide10.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, x + Inches(0.2), Inches(3.1) if not is_top else Inches(3.9), Inches(1.3), Inches(0.35))
        dbadge.fill.solid()
        dbadge.fill.fore_color.rgb = DARK_CARD
        dbadge.line.fill.background()
        dbadge.text_frame.paragraphs[0].text = m_date
        dbadge.text_frame.paragraphs[0].font.size = Pt(9)
        dbadge.text_frame.paragraphs[0].font.bold = True
        dbadge.text_frame.paragraphs[0].font.color.rgb = BRIGHT_GREEN
        dbadge.text_frame.paragraphs[0].alignment = PP_ALIGN.CENTER

        # Content Card (Alternating Top / Bottom)
        card_y = Inches(1.5) if is_top else Inches(4.4)
        c_shape = add_card(slide10, x, card_y, Inches(1.7), Inches(1.5), bg_color=WHITE, border_color=BORDER_COLOR)
        
        tb = slide10.shapes.add_textbox(x + Inches(0.1), card_y + Inches(0.1), Inches(1.5), Inches(1.3))
        tf = tb.text_frame
        tf.word_wrap = True
        p_t = tf.paragraphs[0]
        p_t.text = m_title
        p_t.font.size = Pt(10)
        p_t.font.bold = True
        p_t.font.color.rgb = DARK_TEXT

        p_d = tf.add_paragraph()
        p_d.text = m_desc
        p_d.font.size = Pt(8)
        p_d.font.color.rgb = MUTED_TEXT
        p_d.font.size = Pt(8)
        p_d.font.color.rgb = MUTED_TEXT

    # =========================================================================
    # SLIDE 11: USER 1 (Smallholder Farmer Persona)
    # =========================================================================
    slide11 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide11, LIGHT_BG)
    add_header(slide11, "USER PERSONA 1: SMALLHOLDER CULTIVATOR")

    # Left: Profile card
    add_card(slide11, Inches(0.8), Inches(1.4), Inches(3.6), Inches(5.4), bg_color=DARK_CARD, border_color=BRIGHT_GREEN)
    
    # Profile avatar circle
    av = slide11.shapes.add_shape(MSO_SHAPE.OVAL, Inches(2.0), Inches(1.8), Inches(1.2), Inches(1.2))
    av.fill.solid()
    av.fill.fore_color.rgb = ACCENT_GREEN
    av.line.fill.background()

    tb_p = slide11.shapes.add_textbox(Inches(1.0), Inches(3.2), Inches(3.2), Inches(3.4))
    tf_p = tb_p.text_frame
    tf_p.word_wrap = True

    pp0 = tf_p.paragraphs[0]
    pp0.text = "MURUGAN K."
    pp0.font.size = Pt(18)
    pp0.font.bold = True
    pp0.font.color.rgb = WHITE
    pp0.alignment = PP_ALIGN.CENTER
    pp0.space_after = Pt(4)

    pp1 = tf_p.add_paragraph()
    pp1.text = "Age: 48 | Land: 3.5 Acres\nLocation: Thanjavur, Tamil Nadu\nCrops: Paddy (Samba) & Sugarcane\nDevice: Budget Android (4G/Offline)"
    pp1.font.size = Pt(10)
    pp1.font.color.rgb = RGBColor(210, 230, 215)
    pp1.alignment = PP_ALIGN.CENTER

    # Right: Goal, Frustration & Problem Statement
    add_card(slide11, Inches(4.7), Inches(1.4), Inches(7.8), Inches(5.4), bg_color=WHITE, border_color=BORDER_COLOR)
    tb_u1 = slide11.shapes.add_textbox(Inches(5.0), Inches(1.6), Inches(7.2), Inches(5.0))
    tf_u1 = tb_u1.text_frame
    tf_u1.word_wrap = True

    def add_persona_section(tf, label, text, color):
        p_l = tf.add_paragraph() if tf.paragraphs[0].text else tf.paragraphs[0]
        p_l.text = label
        p_l.font.size = Pt(12)
        p_l.font.bold = True
        p_l.font.color.rgb = color
        p_l.space_after = Pt(2)

        p_t = tf.add_paragraph()
        p_t.text = text
        p_t.font.size = Pt(10.5)
        p_t.font.color.rgb = DARK_TEXT
        p_t.space_after = Pt(10)

    add_persona_section(tf_u1, "USER GOAL :", 
        "Murugan wants to optimize his daily irrigation schedule and detect crop stress early to prevent yield loss. He seeks clear, trustworthy guidance in Tamil without spending hours deciphering scientific jargon or relying on patchy mobile connectivity in his paddy fields.", ACCENT_GREEN)

    add_persona_section(tf_u1, "DAILY FRUSTRATION :", 
        "• Power supply to his agricultural pump is erratic (3-phase power arrives unpredictably).\n• High cellular data latency causes existing weather/farm apps to hang or fail in the field.\n• He often over-irrigates out of habit, which wastes valuable groundwater and leaches fertilizers.\n• Middlemen pay substandard rates at the local market because he lacks daily price benchmarks.", RGBColor(211, 47, 47))

    add_persona_section(tf_u1, "PROBLEM STATEMENT & GEOPULSE FIT :", 
        "Smallholder farmers operating in low-connectivity rural belts face severe water insecurity and yield penalties due to guesswork. GeoPulse solves Murugan's challenges by:\n✔ Providing an exact daily pump schedule (e.g., 'Run 5HP pump for 42 minutes') via FAO-56 modeling.\n✔ Offering complete offline accessibility with automatic background sync when returning to town.\n✔ Visualizing field health using satellite NDVI color zones translated into Tamil action alerts.", DARK_TEXT)

    # =========================================================================
    # SLIDE 12: USER 2 (Agronomist / Extension Officer Persona)
    # =========================================================================
    slide12 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide12, LIGHT_BG)
    add_header(slide12, "USER PERSONA 2: FPO FIELD AGRONOMIST")

    # Left: Profile card
    add_card(slide12, Inches(0.8), Inches(1.4), Inches(3.6), Inches(5.4), bg_color=DARK_CARD, border_color=BRIGHT_GREEN)
    
    av2 = slide12.shapes.add_shape(MSO_SHAPE.OVAL, Inches(2.0), Inches(1.8), Inches(1.2), Inches(1.2))
    av2.fill.solid()
    av2.fill.fore_color.rgb = ACCENT_GOLD
    av2.line.fill.background()

    tb_p2 = slide12.shapes.add_textbox(Inches(1.0), Inches(3.2), Inches(3.2), Inches(3.4))
    tf_p2 = tb_p2.text_frame
    tf_p2.word_wrap = True

    p20 = tf_p2.paragraphs[0]
    p20.text = "DR. ANANYA S."
    p20.font.size = Pt(18)
    p20.font.bold = True
    p20.font.color.rgb = WHITE
    p20.alignment = PP_ALIGN.CENTER
    p20.space_after = Pt(4)

    p21 = tf_p2.add_paragraph()
    p21.text = "Age: 29 | Role: Lead Agronomist\nOrganization: Kaveri Delta FPO (120+ Farmers)\nLocation: Coimbatore & Erode, Tamil Nadu\nFocus: Precision GIS & Sustainable Cultivation"
    p21.font.size = Pt(10)
    p21.font.color.rgb = RGBColor(210, 230, 215)
    p21.alignment = PP_ALIGN.CENTER

    # Right: Details
    add_card(slide12, Inches(4.7), Inches(1.4), Inches(7.8), Inches(5.4), bg_color=WHITE, border_color=BORDER_COLOR)
    tb_u2 = slide12.shapes.add_textbox(Inches(5.0), Inches(1.6), Inches(7.2), Inches(5.0))
    tf_u2 = tb_u2.text_frame
    tf_u2.word_wrap = True

    add_persona_section(tf_u2, "USER GOAL :", 
        "Dr. Ananya manages over 120 member farm plots. Her goal is to remotely scout cluster-wide crop health using satellite vegetation indices (NDVI/NDWI) and prioritize in-person visits to plots experiencing moisture stress or pest outbreaks, while advising on optimum harvest windows.", ACCENT_GREEN)

    add_persona_section(tf_u2, "DAILY FRUSTRATION :", 
        "• Visiting every member parcel physically takes days and is logistically unsustainable.\n• Commercial GIS tools (e.g. ArcGIS) require desktop workstations and cannot be used during field visits.\n• Difficulty tracking historical crop phenology and fertilizer responses across diverse soil types.\n• Fragmented mandi price data leads to disjointed marketing decisions across the FPO.", RGBColor(211, 47, 47))

    add_persona_section(tf_u2, "PROBLEM STATEMENT & GEOPULSE FIT :", 
        "Agricultural extension officers lack lightweight, mobile-first GIS tools that summarize farm health across multiple polygon boundaries. GeoPulse empowers Dr. Ananya with:\n✔ Interactive MapLibre GIS satellite map for multi-field polygon boundary monitoring.\n✔ Automated STAC Sentinel-2 & ISRO Bhoonidhi spectral analysis updated on every satellite pass.\n✔ Live AGMARKNET commodity rate integration to advise FPO members on optimal harvest selling.", DARK_TEXT)

    # =========================================================================
    # SLIDE 13: USER JOURNEY MAP
    # =========================================================================
    slide13 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide13, LIGHT_BG)
    add_header(slide13, "USER JOURNEY MAP: SMALLHOLDER FARMER WORKFLOW")

    # Table for Journey Map
    jm_table_shape = slide13.shapes.add_table(6, 4, Inches(0.8), Inches(1.5), Inches(11.7), Inches(5.3))
    jm_table = jm_table_shape.table
    jm_table.columns[0].width = Inches(2.2)
    jm_table.columns[1].width = Inches(4.2)
    jm_table.columns[2].width = Inches(2.3)
    jm_table.columns[3].width = Inches(3.0)

    jm_headers = ["Action / Stage", "Task List & User Interaction", "Feeling & Mindset", "Improvement Opportunities"]
    for i, h in enumerate(jm_headers):
        cell = jm_table.cell(0, i)
        cell.fill.solid()
        cell.fill.fore_color.rgb = DARK_BG
        cell.text_frame.paragraphs[0].text = h
        cell.text_frame.paragraphs[0].font.size = Pt(10.5)
        cell.text_frame.paragraphs[0].font.bold = True
        cell.text_frame.paragraphs[0].font.color.rgb = WHITE
        cell.text_frame.paragraphs[0].alignment = PP_ALIGN.CENTER

    journey_rows = [
        ("1. Plot Boundary Setup", "Open app, select Tamil, draw GPS farm boundary polygon on satellite basemap.", "Curious & Hopeful", "Add cadastral survey number lookup and auto-polygon GPS walk mode."),
        ("2. Satellite Crop Inspection", "View latest Sentinel-2 NDVI overlay, check vegetation vigor and anomaly zones.", "Empowered & Informed", "Provide high-contrast color zoning (Green/Yellow/Red) for direct sunlight readability."),
        ("3. Irrigation Check (FAO-56)", "Review daily water deficit (mm) and required pump operating time in minutes.", "Relieved & Decisive", "Include direct pump horse-power (HP) and drip emitter runtime calculator."),
        ("4. Offline Field Usage", "Review cached agronomic advisories and weather radar while deep in offline field plots.", "Secure & Autonomous", "Optimize SQLite caching and display explicit 'Offline Protected' status badge."),
        ("5. Mandi Market Tracking", "Check daily AGMARKNET commodity trading prices before finalizing harvest dates.", "Valued & Competitive", "Add price surge alerts and distance-to-market logistics cost estimations.")
    ]

    for row_idx, (stage, tasks, feeling, opps) in enumerate(journey_rows, start=1):
        c0 = jm_table.cell(row_idx, 0)
        c0.fill.solid()
        c0.fill.fore_color.rgb = WHITE
        c0.text_frame.paragraphs[0].text = stage
        c0.text_frame.paragraphs[0].font.size = Pt(9.5)
        c0.text_frame.paragraphs[0].font.bold = True
        c0.text_frame.paragraphs[0].font.color.rgb = ACCENT_GREEN

        c1 = jm_table.cell(row_idx, 1)
        c1.fill.solid()
        c1.fill.fore_color.rgb = WHITE
        c1.text_frame.paragraphs[0].text = tasks
        c1.text_frame.paragraphs[0].font.size = Pt(9)
        c1.text_frame.paragraphs[0].font.color.rgb = DARK_TEXT

        c2 = jm_table.cell(row_idx, 2)
        c2.fill.solid()
        c2.fill.fore_color.rgb = RGBColor(240, 248, 242)
        c2.text_frame.paragraphs[0].text = feeling
        c2.text_frame.paragraphs[0].font.size = Pt(9)
        c2.text_frame.paragraphs[0].font.bold = True
        c2.text_frame.paragraphs[0].font.color.rgb = ACCENT_GOLD if "Curious" in feeling else ACCENT_GREEN

        c3 = jm_table.cell(row_idx, 3)
        c3.fill.solid()
        c3.fill.fore_color.rgb = WHITE
        c3.text_frame.paragraphs[0].text = opps
        c3.text_frame.paragraphs[0].font.size = Pt(8.5)
        c3.text_frame.paragraphs[0].font.color.rgb = MUTED_TEXT

    # =========================================================================
    # SLIDE 14: COMPETITIVE ANALYSIS
    # =========================================================================
    slide14 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide14, LIGHT_BG)
    add_header(slide14, "COMPETITIVE ANALYSIS: BENCHMARKING & ADVANTAGES")

    ca_table_shape = slide14.shapes.add_table(5, 4, Inches(0.8), Inches(1.5), Inches(11.7), Inches(5.3))
    ca_table = ca_table_shape.table
    ca_table.columns[0].width = Inches(2.2)
    ca_table.columns[1].width = Inches(3.2)
    ca_table.columns[2].width = Inches(3.2)
    ca_table.columns[3].width = Inches(3.1)

    ca_headers = ["Platform / Competitor", "Key Strengths", "Critical Limitations", "GeoPulse Advantage"]
    for i, h in enumerate(ca_headers):
        cell = ca_table.cell(0, i)
        cell.fill.solid()
        cell.fill.fore_color.rgb = DARK_BG
        cell.text_frame.paragraphs[0].text = h
        cell.text_frame.paragraphs[0].font.size = Pt(10.5)
        cell.text_frame.paragraphs[0].font.bold = True
        cell.text_frame.paragraphs[0].font.color.rgb = WHITE
        cell.text_frame.paragraphs[0].alignment = PP_ALIGN.CENTER

    competitors = [
        ("Cropin SmartFarm", "Established B2B enterprise farm management, extensive supply chain traceability.", "Prohibitive corporate pricing; cloud-dependent; no native offline vernacular support for smallholders.", "100% free open-access data pipeline, offline-first Room cache, and smallholder focus."),
        ("Plantix", "Image-based leaf disease diagnosis with large visual image recognition dataset.", "Requires constant high-speed connection; reactive rather than predictive; lacks GIS satellite integration.", "Proactive Sentinel-2 satellite NDVI scouting with proactive FAO-56 irrigation planning."),
        ("DeHaat / AgroStar", "Extensive input marketplace (seeds/fertilizers), large rural distribution network.", "Monetizes platform by selling agri-chemicals; advisory may be biased toward product sales.", "Purely unbiased, scientific agronomic guidance with zero commercial chemical sales conflicts."),
        ("Govt Portals (eNAM / Agrisnet)", "Authoritative mandi rates, official government subsidy schemes and announcements.", "Clunky legacy UI, unreliable server downtimes, lack of automated geospatial boundary tools.", "Native Android Jetpack Compose UI, MapLibre GIS farm boundaries, and automated AGMARKNET parsing.")
    ]

    for row_idx, (comp, strn, lim, adv) in enumerate(competitors, start=1):
        c0 = ca_table.cell(row_idx, 0)
        c0.fill.solid()
        c0.fill.fore_color.rgb = WHITE
        c0.text_frame.paragraphs[0].text = comp
        c0.text_frame.paragraphs[0].font.size = Pt(10)
        c0.text_frame.paragraphs[0].font.bold = True
        c0.text_frame.paragraphs[0].font.color.rgb = DARK_TEXT

        c1 = ca_table.cell(row_idx, 1)
        c1.fill.solid()
        c1.fill.fore_color.rgb = WHITE
        c1.text_frame.paragraphs[0].text = strn
        c1.text_frame.paragraphs[0].font.size = Pt(9)
        c1.text_frame.paragraphs[0].font.color.rgb = MUTED_TEXT

        c2 = ca_table.cell(row_idx, 2)
        c2.fill.solid()
        c2.fill.fore_color.rgb = RGBColor(254, 245, 245)
        c2.text_frame.paragraphs[0].text = lim
        c2.text_frame.paragraphs[0].font.size = Pt(9)
        c2.text_frame.paragraphs[0].font.color.rgb = RGBColor(198, 40, 40)

        c3 = ca_table.cell(row_idx, 3)
        c3.fill.solid()
        c3.fill.fore_color.rgb = RGBColor(240, 248, 242)
        c3.text_frame.paragraphs[0].text = adv
        c3.text_frame.paragraphs[0].font.size = Pt(9)
        c3.text_frame.paragraphs[0].font.bold = True
        c3.text_frame.paragraphs[0].font.color.rgb = ACCENT_GREEN

    # =========================================================================
    # SLIDE 15: BUSINESS POTENTIAL
    # =========================================================================
    slide15 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide15, LIGHT_BG)
    add_header(slide15, "BUSINESS POTENTIAL & MARKET OPPORTUNITY")

    # Left Narrative Card
    add_card(slide15, Inches(0.8), Inches(1.4), Inches(6.0), Inches(5.4), bg_color=WHITE, border_color=BORDER_COLOR)
    tb_bp = slide15.shapes.add_textbox(Inches(1.1), Inches(1.6), Inches(5.4), Inches(5.0))
    tf_bp = tb_bp.text_frame
    tf_bp.word_wrap = True

    pbp0 = tf_bp.paragraphs[0]
    pbp0.text = "TARGET AUDIENCE DISTRIBUTION"
    pbp0.font.size = Pt(13)
    pbp0.font.bold = True
    pbp0.font.color.rgb = ACCENT_GREEN
    pbp0.space_after = Pt(8)

    pbp1 = tf_bp.add_paragraph()
    pbp1.text = "The GeoPulse platform addresses a high-growth agritech sector driven by the national push for climate-resilient agriculture, digital crop surveys, and water sustainability in India:"
    pbp1.font.size = Pt(11)
    pbp1.font.color.rgb = DARK_TEXT
    pbp1.space_after = Pt(8)

    segments = [
        ("Smallholder Farmers (40%)", "Over 120 million small & marginal cultivators seeking water conservation and early disease alerts without expensive hardware."),
        ("Commercial Cultivators & FPOs (30%)", "Farmer Producer Organizations aggregating acreage to optimize bulk input procurement, harvest scheduling, and APMC marketing."),
        ("Agri-Extension & Field Officers (20%)", "Government and NGO agronomists monitoring village clusters and providing scientifically verified crop advisories."),
        ("Agri-Fintech & Insurance Orgs (10%)", "Credit assessors and crop insurance underwriters requiring historical satellite NDVI telemetry to evaluate yield risk.")
    ]
    for seg_title, seg_desc in segments:
        p_st = tf_bp.add_paragraph()
        p_st.text = "• " + seg_title
        p_st.font.size = Pt(10.5)
        p_st.font.bold = True
        p_st.font.color.rgb = DARK_TEXT
        
        p_sd = tf_bp.add_paragraph()
        p_sd.text = "  " + seg_desc
        p_sd.font.size = Pt(9.5)
        p_sd.font.color.rgb = MUTED_TEXT
        p_sd.space_after = Pt(6)

    # Right Revenue Impact Card
    add_card(slide15, Inches(7.1), Inches(1.4), Inches(5.4), Inches(5.4), bg_color=WHITE, border_color=BORDER_COLOR)
    tb_rev = slide15.shapes.add_textbox(Inches(7.4), Inches(1.6), Inches(4.8), Inches(5.0))
    tf_rev = tb_rev.text_frame
    tf_rev.word_wrap = True

    pr0 = tf_rev.paragraphs[0]
    pr0.text = "ECONOMIC IMPACT & ADOPTION DRIVERS"
    pr0.font.size = Pt(13)
    pr0.font.bold = True
    pr0.font.color.rgb = BRIGHT_GREEN
    pr0.space_after = Pt(8)

    pr_bullets = [
        ("Groundwater Savings (~35%)", "Scientific FAO-56 irrigation scheduling reduces agricultural power consumption and prevents localized aquifer depletion."),
        ("Yield Optimization (+18-25%)", "Early satellite vegetation anomaly detection allows prompt intervention before pest infestations or moisture deficits become irreversible."),
        ("Middlemen Margin Reduction", "Real-time AGMARKNET mandi rates give cultivators direct bargaining power, enhancing realized farm-gate prices by 8-15%."),
        ("B2B SaaS / FPO Enterprise Model", "Freemium for smallholders; premium analytics subscription for FPOs and research institutions managing large contiguous farm acreage.")
    ]
    for r_title, r_desc in pr_bullets:
        p = tf_rev.add_paragraph()
        p.text = "✔ " + r_title
        p.font.size = Pt(11)
        p.font.bold = True
        p.font.color.rgb = DARK_TEXT
        p.space_after = Pt(2)

        pd = tf_rev.add_paragraph()
        pd.text = r_desc
        pd.font.size = Pt(9.5)
        pd.font.color.rgb = MUTED_TEXT
        pd.space_after = Pt(8)

    # =========================================================================
    # SLIDE 16: BUSINESS POTENTIAL - AUDIENCE & REVENUE ANALYSIS
    # =========================================================================
    slide16 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide16, LIGHT_BG)
    add_header(slide16, "BUSINESS POTENTIAL: AUDIENCE & REVENUE ANALYSIS")

    # 4 Structured KPI Cards
    kpis = [
        ("Smallholder Farmers", "40%", "₹ 16,500 Cr", "Broadest base; highest volume of daily active sessions and grassroots impact."),
        ("FPOs & Cooperatives", "30%", "₹ 12,400 Cr", "High willingness to pay for multi-plot GIS dashboard and harvest logistics."),
        ("Agri-Extension & NGOs", "20%", "₹ 8,200 Cr", "Institutional deployment for rural development and climate advisory missions."),
        ("Agri-Fintech & Insurance", "10%", "₹ 4,800 Cr", "Satellite telemetry API queries for verifiable crop insurance claims.")
    ]

    kw = Inches(2.7)
    kh = Inches(4.5)
    for i, (cat, pct, rev, detail) in enumerate(kpis):
        kx = Inches(0.8) + (i * Inches(2.95))
        ky = Inches(1.5)
        add_card(slide16, kx, ky, kw, kh, bg_color=WHITE, border_color=BORDER_COLOR)

        tb_k = slide16.shapes.add_textbox(kx + Inches(0.2), ky + Inches(0.3), kw - Inches(0.4), kh - Inches(0.6))
        tf_k = tb_k.text_frame
        tf_k.word_wrap = True

        pk_t = tf_k.paragraphs[0]
        pk_t.text = cat.upper()
        pk_t.font.size = Pt(11)
        pk_t.font.bold = True
        pk_t.font.color.rgb = DARK_TEXT
        pk_t.space_after = Pt(10)

        pk_p = tf_k.add_paragraph()
        pk_p.text = pct
        pk_p.font.size = Pt(32)
        pk_p.font.bold = True
        pk_p.font.color.rgb = ACCENT_GREEN
        pk_p.space_after = Pt(4)

        pk_l = tf_k.add_paragraph()
        pk_l.text = "Share of User Base"
        pk_l.font.size = Pt(9)
        pk_l.font.color.rgb = MUTED_TEXT
        pk_l.space_after = Pt(14)

        pk_r = tf_k.add_paragraph()
        pk_r.text = rev
        pk_r.font.size = Pt(18)
        pk_r.font.bold = True
        pk_r.font.color.rgb = ACCENT_GOLD
        pk_r.space_after = Pt(4)

        pk_rl = tf_k.add_paragraph()
        pk_rl.text = "Market Opportunity"
        pk_rl.font.size = Pt(9)
        pk_rl.font.color.rgb = MUTED_TEXT
        pk_rl.space_after = Pt(12)

        pk_d = tf_k.add_paragraph()
        pk_d.text = detail
        pk_d.font.size = Pt(9.5)
        pk_d.font.color.rgb = DARK_TEXT

    # Summary bar at bottom
    sbar = add_card(slide16, Inches(0.8), Inches(6.2), Inches(11.7), Inches(0.8), bg_color=DARK_CARD, border_color=BRIGHT_GREEN)
    tb_sb = slide16.shapes.add_textbox(Inches(1.0), Inches(6.3), Inches(11.3), Inches(0.6))
    tf_sb = tb_sb.text_frame
    tf_sb.paragraphs[0].text = "TOTAL ADDRESSABLE AGRITECH REVENUE IN INDIA: OVER ₹ 41,900 CRORES BY 2028 (CAGR: 24.3%)"
    tf_sb.paragraphs[0].font.size = Pt(11.5)
    tf_sb.paragraphs[0].font.bold = True
    tf_sb.paragraphs[0].font.color.rgb = WHITE
    tf_sb.paragraphs[0].alignment = PP_ALIGN.CENTER

    # =========================================================================
    # SLIDE 17: USER PAIN POINTS
    # =========================================================================
    slide17 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide17, LIGHT_BG)
    add_header(slide17, "CORE USER PAIN POINTS IN PRECISION FARMING")

    pain_points = [
        ("Generic & Irrelevant Advice", 
         "One-Size-Fits-All Crop Calendars:",
         "Most conventional farming apps push static generic advice based on statewide seasons, ignoring actual field soil type, localized weather events, and micro-climate variations. Farmers receive identical suggestions whether farming in clay loam or sandy soil, leading to lost confidence and rapid app abandonment.",
         RGBColor(211, 47, 47)),

        ("Satellite Opacity & Complex Jargon", 
         "Unusable Scientific Telemetry:",
         "While satellite data is publicly funded (Copernicus & ISRO), existing tools show raw multi-spectral decimals (e.g. 'NDVI = 0.63') without actionable context. Farmers cannot tell whether that means their paddy needs irrigation, pesticide, or fertilizer, resulting in severe cognitive overload.",
         ACCENT_GOLD),

        ("Network Sensitivity & Inaccessibility", 
         "Fragility in Rural Dead Zones:",
         "Farms are naturally located far from urban telecommunication towers. When cellular 4G signals degrade to 2G or drop entirely, cloud-heavy apps crash, hang, or refuse to render maps. Cultivators are left completely without guidance during critical on-field operations.",
         DARK_TEXT)
    ]

    pw = Inches(3.7)
    ph = Inches(5.2)
    for i, (title, sub, body, color) in enumerate(pain_points):
        px = Inches(0.8) + (i * Inches(4.0))
        py = Inches(1.5)
        add_card(slide17, px, py, pw, ph, bg_color=WHITE, border_color=BORDER_COLOR)

        # Color header bar
        hbar = slide17.shapes.add_shape(MSO_SHAPE.RECTANGLE, px, py, pw, Inches(0.15))
        hbar.fill.solid()
        hbar.fill.fore_color.rgb = color
        hbar.line.fill.background()

        tb_p = slide17.shapes.add_textbox(px + Inches(0.25), py + Inches(0.3), pw - Inches(0.5), ph - Inches(0.5))
        tf_p = tb_p.text_frame
        tf_p.word_wrap = True

        pt0 = tf_p.paragraphs[0]
        pt0.text = f"PAIN POINT {i+1}"
        pt0.font.size = Pt(10)
        pt0.font.bold = True
        pt0.font.color.rgb = color
        pt0.space_after = Pt(4)

        pt1 = tf_p.add_paragraph()
        pt1.text = title
        pt1.font.size = Pt(14)
        pt1.font.bold = True
        pt1.font.color.rgb = DARK_TEXT
        pt1.space_after = Pt(8)

        pt2 = tf_p.add_paragraph()
        pt2.text = sub
        pt2.font.size = Pt(10.5)
        pt2.font.bold = True
        pt2.font.color.rgb = MUTED_TEXT
        pt2.space_after = Pt(8)

        pt3 = tf_p.add_paragraph()
        pt3.text = body
        pt3.font.size = Pt(10)
        pt3.font.color.rgb = DARK_TEXT

    # =========================================================================
    # SLIDE 18: FLOW DIAGRAM / SYSTEM ARCHITECTURE
    # =========================================================================
    slide18 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide18, LIGHT_BG)
    add_header(slide18, "END-TO-END SYSTEM ARCHITECTURE & DATA FLOW")

    # Workflow Pipeline Cards (Horizontal sequence)
    flow_steps = [
        ("1. User Client", "Android Jetpack Compose\n• Language Selection (Tamil/En)\n• User Auth & Farm Profile\n• Offline Cache Sync"),
        ("2. Geospatial GIS", "MapLibre Mobile SDK\n• GPS Polygon Field Drawing\n• Field Boundary Geofencing\n• Offline Vector Basemap"),
        ("3. Backend Engine", "FastAPI Asynchronous API\n• Coordinates Parsing\n• Agro-meteo Assimilation\n• Crop Phenology Lookup"),
        ("4. Satellite Ingestion", "STAC Raster Pipeline\n• Copernicus Sentinel-2 L2A\n• ISRO Bhoonidhi Catalog\n• NDVI/NDWI Extraction"),
        ("5. Scientific Core", "FAO-56 Water Balance\n• Penman-Monteith ETo\n• USDA-SCS Rain Runoff\n• Daily Pump Dose Calc"),
        ("6. Decision Output", "Actionable Guidance\n• Bilingual Tamil/English Cards\n• AGMARKNET Mandi Rates\n• Room SQLite Persistent DB")
    ]

    flow_w = Inches(1.8)
    flow_h = Inches(4.8)
    for i, (stitle, sbody) in enumerate(flow_steps):
        fx = Inches(0.8) + (i * Inches(1.98))
        fy = Inches(1.6)
        c_shape = add_card(slide18, fx, fy, flow_w, flow_h, bg_color=WHITE, border_color=BORDER_COLOR)

        # Step indicator badge
        sbadge = slide18.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, fx + Inches(0.15), fy + Inches(0.2), flow_w - Inches(0.3), Inches(0.4))
        sbadge.fill.solid()
        sbadge.fill.fore_color.rgb = ACCENT_GREEN if i in [0, 4, 5] else DARK_CARD
        sbadge.line.fill.background()
        sbadge.text_frame.paragraphs[0].text = stitle
        sbadge.text_frame.paragraphs[0].font.size = Pt(9.5)
        sbadge.text_frame.paragraphs[0].font.bold = True
        sbadge.text_frame.paragraphs[0].font.color.rgb = WHITE
        sbadge.text_frame.paragraphs[0].alignment = PP_ALIGN.CENTER

        tb_f = slide18.shapes.add_textbox(fx + Inches(0.12), fy + Inches(0.8), flow_w - Inches(0.24), flow_h - Inches(0.9))
        tf_f = tb_f.text_frame
        tf_f.word_wrap = True
        for line_idx, line_txt in enumerate(sbody.split("\n")):
            p = tf_f.paragraphs[0] if line_idx == 0 else tf_f.add_paragraph()
            p.text = line_txt
            if "•" in line_txt:
                p.font.size = Pt(8.5)
                p.font.color.rgb = DARK_TEXT
                p.space_after = Pt(4)
            else:
                p.font.size = Pt(10)
                p.font.bold = True
                p.font.color.rgb = ACCENT_GREEN
                p.space_after = Pt(6)

        # Connector arrow to next step
        if i < len(flow_steps) - 1:
            arr = slide18.shapes.add_shape(MSO_SHAPE.RIGHT_ARROW, fx + flow_w + Inches(0.02), fy + Inches(2.2), Inches(0.14), Inches(0.25))
            arr.fill.solid()
            arr.fill.fore_color.rgb = BRIGHT_GREEN
            arr.line.fill.background()

    # =========================================================================
    # SLIDE 19: PRODUCT SKETCHES & WIREFRAME CONCEPTS
    # =========================================================================
    slide19 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide19, LIGHT_BG)
    add_header(slide19, "PRODUCT SKETCHES & WIREFRAME BLUEPRINTS")

    wireframes = [
        ("Screen 1: Welcome & Language", "Bilingual entry gateway allowing farmers to switch between Tamil and English. Clean logo, high-contrast CTA buttons, and offline session restore."),
        ("Screen 2: Interactive Field Map", "MapLibre GPS-enabled satellite map. Farmers tap vertices to define their farm polygon boundary, calculating exact acreage automatically."),
        ("Screen 3: Crop Health Dashboard", "NDVI & NDWI vegetation index cards. Ranks field health into Green (Optimal), Yellow (Moderate), and Red (Moisture Stress) with zero dummy data."),
        ("Screen 4: Scientific Irrigation", "FAO-56 daily water balance summary. Displays current soil deficit in millimeters and converts to practical pump running time in minutes."),
        ("Screen 5: Mandi Market Rates", "Direct AGMARKNET commodity rate monitor. Displays minimum, modal, and maximum crop rates across nearby APMC mandis to guide harvest timing.")
    ]

    ww = Inches(2.2)
    wh = Inches(5.0)
    for i, (wtitle, wdesc) in enumerate(wireframes):
        wx = Inches(0.8) + (i * Inches(2.38))
        wy = Inches(1.6)

        # Phone mockup outline
        phone = add_card(slide19, wx, wy, ww, wh, bg_color=WHITE, border_color=DARK_TEXT)
        
        # Phone notch/speaker
        speaker = slide19.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, wx + Inches(0.7), wy + Inches(0.15), Inches(0.8), Inches(0.08))
        speaker.fill.solid()
        speaker.fill.fore_color.rgb = RGBColor(200, 200, 200)
        speaker.line.fill.background()

        tb_w = slide19.shapes.add_textbox(wx + Inches(0.15), wy + Inches(0.4), ww - Inches(0.3), wh - Inches(0.6))
        tf_w = tb_w.text_frame
        tf_w.word_wrap = True

        pw0 = tf_w.paragraphs[0]
        pw0.text = wtitle
        pw0.font.size = Pt(10.5)
        pw0.font.bold = True
        pw0.font.color.rgb = ACCENT_GREEN
        pw0.space_after = Pt(8)

        pw1 = tf_w.add_paragraph()
        pw1.text = wdesc
        pw1.font.size = Pt(9)
        pw1.font.color.rgb = DARK_TEXT

    # =========================================================================
    # SLIDE 20: GEOPULSE APP SCREENS & UI IMPLEMENTATION
    # =========================================================================
    slide20 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide20, LIGHT_BG)
    add_header(slide20, "NATIVE JETPACK COMPOSE UI IMPLEMENTATION")

    screen_breakdown = [
        ("Authentication & Onboarding", "SplashActivity, WelcomeActivity, LanguageSelectActivity, LoginActivity, CreateAccountActivity", 
         "Delivers instantaneous app launch, animated branded intro, bilingual toggle (English/Tamil), and secure SharedPreferences token persistence."),
        ("Home Agronomy Dashboard", "Screen7Activity (Home Dashboard), GeoBottomBar, Soil Moisture Meter, Weather Radar", 
         "Real-time agronomic telemetry featuring multi-field dynamic switching, live evapotranspiration meters, and 6-question vernacular action guide."),
        ("Geospatial Satellite Farm Map", "Screen8Activity, MapLibreGisMap, InteractiveSatelliteFarmMap", 
         "Full-screen interactive GIS mapping with GPS location tracking, cadastral polygon drawing, multi-spectral band overlays, and offline raster caching."),
        ("Scientific Agronomy & Irrigation", "Screen11 (NDVI), Screen12 (Soil Moisture), Screen19 (FAO-56 Irrigation Engine)", 
         "FAO-56 daily water balance calculator calculating root-zone soil water depletion and generating pump runtime advisories based on pump HP."),
        ("Market Intelligence & Profile", "Screen26 (AGMARKNET Mandi Rates), Screen10 (Settings & Profile), NotificationDialog", 
         "Live commodity pricing across Tamil Nadu and national APMC mandis, bilingual notification center, and localized data privacy management.")
    ]

    sb_table_shape = slide20.shapes.add_table(6, 3, Inches(0.8), Inches(1.5), Inches(11.7), Inches(5.3))
    sb_table = sb_table_shape.table
    sb_table.columns[0].width = Inches(2.8)
    sb_table.columns[1].width = Inches(3.6)
    sb_table.columns[2].width = Inches(5.3)

    sb_headers = ["Functional UI Module", "Activity Files & Jetpack Components", "Agronomic & Technical Capabilities"]
    for i, h in enumerate(sb_headers):
        cell = sb_table.cell(0, i)
        cell.fill.solid()
        cell.fill.fore_color.rgb = DARK_BG
        cell.text_frame.paragraphs[0].text = h
        cell.text_frame.paragraphs[0].font.size = Pt(10.5)
        cell.text_frame.paragraphs[0].font.bold = True
        cell.text_frame.paragraphs[0].font.color.rgb = WHITE
        cell.text_frame.paragraphs[0].alignment = PP_ALIGN.CENTER

    for row_idx, (mod, acts, caps) in enumerate(screen_breakdown, start=1):
        c0 = sb_table.cell(row_idx, 0)
        c0.fill.solid()
        c0.fill.fore_color.rgb = WHITE
        c0.text_frame.paragraphs[0].text = mod
        c0.text_frame.paragraphs[0].font.size = Pt(10)
        c0.text_frame.paragraphs[0].font.bold = True
        c0.text_frame.paragraphs[0].font.color.rgb = ACCENT_GREEN

        c1 = sb_table.cell(row_idx, 1)
        c1.fill.solid()
        c1.fill.fore_color.rgb = WHITE
        c1.text_frame.paragraphs[0].text = acts
        c1.text_frame.paragraphs[0].font.size = Pt(8.5)
        c1.text_frame.paragraphs[0].font.color.rgb = MUTED_TEXT

        c2 = sb_table.cell(row_idx, 2)
        c2.fill.solid()
        c2.fill.fore_color.rgb = RGBColor(248, 252, 249)
        c2.text_frame.paragraphs[0].text = caps
        c2.text_frame.paragraphs[0].font.size = Pt(9)
        c2.text_frame.paragraphs[0].font.color.rgb = DARK_TEXT

    # =========================================================================
    # SLIDE 21: USABILITY STUDIES
    # =========================================================================
    slide21 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide21, LIGHT_BG)
    add_header(slide21, "USABILITY STUDIES & ITERATIVE REFINEMENTS")

    # Round 1 Card
    add_card(slide21, Inches(0.8), Inches(1.4), Inches(5.6), Inches(5.4), bg_color=WHITE, border_color=BORDER_COLOR)
    tb_r1 = slide21.shapes.add_textbox(Inches(1.1), Inches(1.6), Inches(5.0), Inches(5.0))
    tf_r1 = tb_r1.text_frame
    tf_r1.word_wrap = True

    pr1_0 = tf_r1.paragraphs[0]
    pr1_0.text = "ROUND 1: INITIAL FIELD STUDY"
    pr1_0.font.size = Pt(13)
    pr1_0.font.bold = True
    pr1_0.font.color.rgb = RGBColor(211, 47, 47)
    pr1_0.space_after = Pt(6)

    pr1_part = tf_r1.add_paragraph()
    pr1_part.text = "Participants: 7 Farmers & 2 Field Officers (Thanjavur & Coimbatore)"
    pr1_part.font.size = Pt(9.5)
    pr1_part.font.bold = True
    pr1_part.font.color.rgb = MUTED_TEXT
    pr1_part.space_after = Pt(10)

    r1_points = [
        ("Confusion Over Decimals:", "Farmers did not understand raw NDVI float values (e.g. 0.48). They wanted simple health terms."),
        ("Network Disconnects:", "When moving between farm bunds where cellular signals dropped, the app hung while waiting for cloud APIs."),
        ("Navigation Friction:", "Farmers had difficulty switching between soil moisture charts and weather radar without an explicit bottom navigation bar."),
        ("Key Refinement Made:", "Added persistent Room offline cache, simplified NDVI into color-coded health badges, and introduced native Tamil translations.")
    ]
    for r_title, r_desc in r1_points:
        p = tf_r1.add_paragraph()
        p.text = "• " + r_title + " " + r_desc
        p.font.size = Pt(9.5)
        p.font.color.rgb = DARK_TEXT
        p.space_after = Pt(6)

    # Round 2 Card
    add_card(slide21, Inches(6.8), Inches(1.4), Inches(5.6), Inches(5.4), bg_color=WHITE, border_color=BORDER_COLOR)
    tb_r2 = slide21.shapes.add_textbox(Inches(7.1), Inches(1.6), Inches(5.0), Inches(5.0))
    tf_r2 = tb_r2.text_frame
    tf_r2.word_wrap = True

    pr2_0 = tf_r2.paragraphs[0]
    pr2_0.text = "ROUND 2: REFINED AUDIT STUDY"
    pr2_0.font.size = Pt(13)
    pr2_0.font.bold = True
    pr2_0.font.color.rgb = ACCENT_GREEN
    pr2_0.space_after = Pt(6)

    pr2_part = tf_r2.add_paragraph()
    pr2_part.text = "Participants: 8 New Smallholders & 3 FPO Agronomists"
    pr2_part.font.size = Pt(9.5)
    pr2_part.font.bold = True
    pr2_part.font.color.rgb = MUTED_TEXT
    pr2_part.space_after = Pt(10)

    r2_points = [
        ("100% Offline Reliability:", "App operated flawlessly in complete airplane mode, loading cached field water balances instantly."),
        ("Pump Duration in Minutes:", "Farmers unanimously praised the conversion of mm water deficits into exact pump minutes based on pump HP."),
        ("High-Contrast Visibility:", "Dark slate and vivid green Material 3 theme remained easily legible under direct tropical sunlight."),
        ("Final Recommendation:", "Implement one-tap manual sync button and audio voice readouts for farmers with limited literacy.")
    ]
    for r_title, r_desc in r2_points:
        p = tf_r2.add_paragraph()
        p.text = "✔ " + r_title + " " + r_desc
        p.font.size = Pt(9.5)
        p.font.color.rgb = DARK_TEXT
        p.space_after = Pt(6)

    # =========================================================================
    # SLIDE 22: LIST OF COMPONENTS / TECHNICAL ARCHITECTURE
    # =========================================================================
    slide22 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide22, LIGHT_BG)
    add_header(slide22, "TECHNICAL ARCHITECTURE & LIST OF COMPONENTS")

    comp_table_shape = slide22.shapes.add_table(8, 3, Inches(0.8), Inches(1.5), Inches(11.7), Inches(5.3))
    comp_table = comp_table_shape.table
    comp_table.columns[0].width = Inches(2.6)
    comp_table.columns[1].width = Inches(3.2)
    comp_table.columns[2].width = Inches(5.9)

    comp_headers = ["System Component", "Technology / Framework", "Technical Specifications & Role"]
    for i, h in enumerate(comp_headers):
        cell = comp_table.cell(0, i)
        cell.fill.solid()
        cell.fill.fore_color.rgb = DARK_BG
        cell.text_frame.paragraphs[0].text = h
        cell.text_frame.paragraphs[0].font.size = Pt(10.5)
        cell.text_frame.paragraphs[0].font.bold = True
        cell.text_frame.paragraphs[0].font.color.rgb = WHITE
        cell.text_frame.paragraphs[0].alignment = PP_ALIGN.CENTER

    components = [
        ("Android Client", "Kotlin, Jetpack Compose, Material 3", "Native UI architecture across 30+ dedicated ComponentActivities with Coroutines and StateFlow."),
        ("Geospatial Map Engine", "MapLibre Native Android SDK, Turf.js", "Vector tile rendering, dynamic GPS location centering, and cadastral field polygon boundary drawing."),
        ("Backend Microservice", "FastAPI (Python 3.11), Uvicorn, Pydantic", "High-performance asynchronous REST API handling coordinates, telemetry ingestion, and business logic."),
        ("Satellite Telemetry Pipeline", "Copernicus CDSE, ISRO Bhoonidhi STAC", "Automated STAC API query and raster processing for Sentinel-2 Level-2A surface reflectance rasters."),
        ("Scientific Agronomy Core", "FAO-56 Irrigation Engine, NumPy", "Penman-Monteith daily ETo calculation, USDA-SCS effective precipitation, and root-zone water balance."),
        ("Hyperlocal Weather Adapter", "Open-Meteo Hourly Forecast API", "Parses temperature, relative humidity, solar radiation, and precipitation for real-time ETo calculation."),
        ("Mandi Market Integration", "AGMARKNET National Portal Parser", "Retrieves live daily commodity minimum, modal, and maximum prices across Tamil Nadu APMC mandis.")
    ]

    for row_idx, (cname, ctech, cspec) in enumerate(components, start=1):
        c0 = comp_table.cell(row_idx, 0)
        c0.fill.solid()
        c0.fill.fore_color.rgb = WHITE
        c0.text_frame.paragraphs[0].text = cname
        c0.text_frame.paragraphs[0].font.size = Pt(9.5)
        c0.text_frame.paragraphs[0].font.bold = True
        c0.text_frame.paragraphs[0].font.color.rgb = ACCENT_GREEN

        c1 = comp_table.cell(row_idx, 1)
        c1.fill.solid()
        c1.fill.fore_color.rgb = WHITE
        c1.text_frame.paragraphs[0].text = ctech
        c1.text_frame.paragraphs[0].font.size = Pt(9)
        c1.text_frame.paragraphs[0].font.bold = True
        c1.text_frame.paragraphs[0].font.color.rgb = DARK_TEXT

        c2 = comp_table.cell(row_idx, 2)
        c2.fill.solid()
        c2.fill.fore_color.rgb = RGBColor(248, 252, 249)
        c2.text_frame.paragraphs[0].text = cspec
        c2.text_frame.paragraphs[0].font.size = Pt(8.5)
        c2.text_frame.paragraphs[0].font.color.rgb = MUTED_TEXT

    # =========================================================================
    # SLIDE 23: PROJECT LEARNING & IMPACT
    # =========================================================================
    slide23 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide23, LIGHT_BG)
    add_header(slide23, "PROJECT IMPACT & ACADEMIC LEARNINGS")

    # Left: Project Impact
    add_card(slide23, Inches(0.8), Inches(1.4), Inches(5.6), Inches(5.4), bg_color=WHITE, border_color=BORDER_COLOR)
    tb_imp = slide23.shapes.add_textbox(Inches(1.1), Inches(1.6), Inches(5.0), Inches(5.0))
    tf_imp = tb_imp.text_frame
    tf_imp.word_wrap = True

    pi_h = tf_imp.paragraphs[0]
    pi_h.text = "REAL-WORLD PROJECT IMPACT"
    pi_h.font.size = Pt(13)
    pi_h.font.bold = True
    pi_h.font.color.rgb = ACCENT_GREEN
    pi_h.space_after = Pt(8)

    impacts = [
        ("Water Resource Conservation:", "Applying FAO-56 Penman-Monteith daily root-zone soil moisture balances eliminates uncalibrated over-irrigation, saving up to 35% agricultural freshwater."),
        ("Zero Dummy Data Authenticity:", "Direct live queries to Copernicus Sentinel-2 and ISRO Bhoonidhi STAC APIs deliver verifiable vegetation indices rather than synthetic simulations."),
        ("Smallholder Economic Empowerment:", "Real-time AGMARKNET mandi rates protect smallholders from price depression, boosting net crop income by 8-15%."),
        ("Inclusive Vernacular Accessibility:", "Native Tamil localization and offline-first Room database ensure equitable technology adoption for farmers regardless of connectivity.")
    ]
    for ititle, idesc in impacts:
        p = tf_imp.add_paragraph()
        p.text = "✔ " + ititle + " " + idesc
        p.font.size = Pt(9.5)
        p.font.color.rgb = DARK_TEXT
        p.space_after = Pt(6)

    # Right: What I Learnt
    add_card(slide23, Inches(6.8), Inches(1.4), Inches(5.6), Inches(5.4), bg_color=WHITE, border_color=BORDER_COLOR)
    tb_lrn = slide23.shapes.add_textbox(Inches(7.1), Inches(1.6), Inches(5.0), Inches(5.0))
    tf_lrn = tb_lrn.text_frame
    tf_lrn.word_wrap = True

    pl_h = tf_lrn.paragraphs[0]
    pl_h.text = "WHAT I LEARNT (AI & DATA SCIENCE PERSPECTIVE)"
    pl_h.font.size = Pt(13)
    pl_h.font.bold = True
    pl_h.font.color.rgb = BRIGHT_GREEN
    pl_h.space_after = Pt(8)

    learnings = [
        ("Geospatial Remote Sensing Pipelines:", "Mastered satellite multi-spectral bands (B04 Red, B08 NIR, B11 SWIR), STAC API querying, and raster mathematics for NDVI/NDWI calculation."),
        ("Applied Agronomic Modeling:", "Implemented rigorous mathematical formulations of the FAO-56 Penman-Monteith equation, crop coefficients (Kc), and USDA-SCS effective precipitation models."),
        ("Native Android & Compose Architecture:", "Gained deep proficiency in building multi-activity Android architectures in Kotlin, state management with StateFlow, and MapLibre GIS integration."),
        ("Offline-First Engineering:", "Designed resilient data synchronization models combining Room/SQLite, OkHttp caching interceptors, and background sync workers.")
    ]
    for ltitle, ldesc in learnings:
        p = tf_lrn.add_paragraph()
        p.text = "• " + ltitle + " " + ldesc
        p.font.size = Pt(9.5)
        p.font.color.rgb = DARK_TEXT
        p.space_after = Pt(6)

    # =========================================================================
    # SLIDE 24: PRODUCT DESIGN PATENT / SCIENTIFIC CLAIMS
    # =========================================================================
    slide24 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide24, LIGHT_BG)
    add_header(slide24, "PRODUCT DESIGN PATENT & INNOVATION CLAIMS")

    claims = [
        ("PATENT CLAIM 1: MULTI-SOURCE STAC SATELLITE RASTER CACHING", 
         "A method and mobile architecture for autonomous precision crop monitoring comprising: querying decentralized STAC repositories (Copernicus Sentinel-2 and ISRO Bhoonidhi); extracting multi-spectral surface reflectance bands within a user-defined GPS cadastral polygon; computing localized NDVI and NDWI vegetation health matrices; and encoding the resultant raster indices into an on-device offline vector cache resilient to network disruption."),

        ("PATENT CLAIM 2: DYNAMIC FAO-56 PENMAN-MONTEITH DAILY WATER BALANCE ENGINE", 
         "A scientific agronomic computation system comprising: continuously ingesting hourly solar radiation, air temperature, relative humidity, and wind speed; calculating reference evapotranspiration (ETo); dynamically determining stage-specific crop coefficients (Kc); computing daily effective precipitation via the USDA-SCS model; and resolving the root-zone soil water depletion balance into an actionable electric pump operating duration."),

        ("PATENT CLAIM 3: LOW-BANDWIDTH VERNACULAR DECISION & MARKET PRICE SYNCHRONIZATION", 
         "An offline-first dual-mode synchronization mechanism comprising: storing field boundaries, soil parameters, and crop phenology records locally in an encrypted SQLite database; serving immediate vernacular (Tamil & English) action advisories; and executing optimistic bidirectional synchronization with AGMARKNET mandi pricing APIs whenever intermittent cellular connectivity is established.")
    ]

    for i, (ctitle, cdesc) in enumerate(claims):
        cy = Inches(1.5) + (i * Inches(1.8))
        add_card(slide24, Inches(0.8), cy, Inches(11.7), Inches(1.6), bg_color=WHITE, border_color=BORDER_COLOR)

        tb_c = slide24.shapes.add_textbox(Inches(1.1), cy + Inches(0.15), Inches(11.1), Inches(1.3))
        tf_c = tb_c.text_frame
        tf_c.word_wrap = True

        pc_t = tf_c.paragraphs[0]
        pc_t.text = ctitle
        pc_t.font.size = Pt(11)
        pc_t.font.bold = True
        pc_t.font.color.rgb = ACCENT_GREEN
        pc_t.space_after = Pt(4)

        pc_d = tf_c.add_paragraph()
        pc_d.text = cdesc
        pc_d.font.size = Pt(9.5)
        pc_d.font.color.rgb = DARK_TEXT

    # =========================================================================
    # SLIDE 25: NEXT STEPS & FUTURE ROADMAP
    # =========================================================================
    slide25 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide25, LIGHT_BG)
    add_header(slide25, "NEXT STEPS & FUTURE DEVELOPMENT ROADMAP")

    roadmap_steps = [
        ("Step A: On-Device Edge AI & Leaf Diagnosis", 
         "• Quantize lightweight TensorFlow Lite computer vision models for on-device pest and leaf disease detection.\n• Allow farmers to snap leaf photos offline and receive instant diagnostic treatment remedies.", ACCENT_GREEN),
        ("Step B: Drone & LoRaWAN IoT Sensor Fusion", 
         "• Integrate low-cost LoRaWAN in-situ capacitive soil moisture probes.\n• Ingest ultra-high-resolution multispectral drone orthomosaics for centimeter-level precision variable rate spraying.", BRIGHT_GREEN),
        ("Step C: Documentation & Patent Filing", 
         "• Prepare comprehensive technical documentation for Indian Patent Office and WIPO provisional patent filing.\n• Publish open-access research benchmark validating FAO-56 water balance accuracy in the Kaveri delta.", ACCENT_GOLD),
        ("Step D: Regulatory, TNAU & Play Store Launch", 
         "• Partner with Tamil Nadu Agricultural University (TNAU) and ICAR Krishi Vigyan Kendras for agronomic certification.\n• Complete Google Play Store release with target deployment across 25 regional Farmer Producer Organizations.", DARK_TEXT)
    ]

    rw = Inches(5.6)
    rh = Inches(2.5)
    rcoords = [
        (Inches(0.8), Inches(1.5)),
        (Inches(6.8), Inches(1.5)),
        (Inches(0.8), Inches(4.3)),
        (Inches(6.8), Inches(4.3))
    ]

    for idx, (stitle, sdesc, scolor) in enumerate(roadmap_steps):
        rx, ry = rcoords[idx]
        add_card(slide25, rx, ry, rw, rh, bg_color=WHITE, border_color=BORDER_COLOR)

        ibar = slide25.shapes.add_shape(MSO_SHAPE.RECTANGLE, rx, ry, Inches(0.15), rh)
        ibar.fill.solid()
        ibar.fill.fore_color.rgb = scolor
        ibar.line.fill.background()

        tb_r = slide25.shapes.add_textbox(rx + Inches(0.35), ry + Inches(0.2), rw - Inches(0.5), rh - Inches(0.4))
        tf_r = tb_r.text_frame
        tf_r.word_wrap = True

        pr_t = tf_r.paragraphs[0]
        pr_t.text = stitle
        pr_t.font.size = Pt(13)
        pr_t.font.bold = True
        pr_t.font.color.rgb = DARK_TEXT
        pr_t.space_after = Pt(6)

        pr_b = tf_r.add_paragraph()
        pr_b.text = sdesc
        pr_b.font.size = Pt(10)
        pr_b.font.color.rgb = MUTED_TEXT

    # =========================================================================
    # SLIDE 26: THANK YOU, LET'S CONNECT (Author Info: Nithish T)
    # =========================================================================
    slide26 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide26, DARK_BG)

    # Accent decorative bar
    bar26 = slide26.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.8), Inches(1.2), Inches(0.3), Inches(4.8))
    bar26.fill.solid()
    bar26.fill.fore_color.rgb = BRIGHT_GREEN
    bar26.line.fill.background()

    tb26 = slide26.shapes.add_textbox(Inches(1.4), Inches(1.2), Inches(11.0), Inches(2.2))
    tf26 = tb26.text_frame
    tf26.word_wrap = True

    p26_0 = tf26.paragraphs[0]
    p26_0.text = "THANK YOU!"
    p26_0.font.size = Pt(40)
    p26_0.font.bold = True
    p26_0.font.color.rgb = WHITE
    p26_0.space_after = Pt(4)

    p26_1 = tf26.add_paragraph()
    p26_1.text = "LET'S CONNECT & COLLABORATE"
    p26_1.font.size = Pt(18)
    p26_1.font.bold = True
    p26_1.font.color.rgb = BRIGHT_GREEN

    # Author card
    add_card(slide26, Inches(1.4), Inches(3.6), Inches(10.5), Inches(3.0), bg_color=DARK_CARD, border_color=BRIGHT_GREEN)
    tb_c26 = slide26.shapes.add_textbox(Inches(1.8), Inches(3.8), Inches(9.8), Inches(2.6))
    tf_c26 = tb_c26.text_frame
    tf_c26.word_wrap = True

    pc26_0 = tf_c26.paragraphs[0]
    pc26_0.text = "NITHISH T"
    pc26_0.font.size = Pt(22)
    pc26_0.font.bold = True
    pc26_0.font.color.rgb = WHITE
    pc26_0.space_after = Pt(4)

    pc26_1 = tf_c26.add_paragraph()
    pc26_1.text = "REG NO:  252514034\nDEPARTMENT: BSC. (HONS) ARTIFICIAL INTELLIGENCE & DATA SCIENCE\nPROJECT: GEOPULSE ANDROID PRECISION AGRONOMY SYSTEM"
    pc26_1.font.size = Pt(13)
    pc26_1.font.color.rgb = RGBColor(220, 235, 225)
    pc26_1.space_after = Pt(10)

    pc26_2 = tf_c26.add_paragraph()
    pc26_2.text = "Repository: GeoPulse (Android Native Jetpack Compose + FastAPI Backend)\nGitHub: github.com/geopulse-app • LinkedIn: linkedin.com/in/nithish-t"
    pc26_2.font.size = Pt(11)
    pc26_2.font.color.rgb = BRIGHT_GREEN

    # Save presentation
    prs.save(output_path)
    print(f"Presentation generated successfully: {output_path}")

if __name__ == "__main__":
    out = "GeoPulse_Project_Presentation.pptx"
    if len(sys.argv) > 1:
        out = sys.argv[1]
    build_presentation(out)
