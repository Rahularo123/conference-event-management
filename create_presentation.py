import os
from pptx import Presentation
from pptx.util import Inches, Pt
from pptx.enum.text import PP_ALIGN
from pptx.dml.color import RGBColor
from pptx.enum.shapes import MSO_SHAPE

prs = Presentation()
prs.slide_width = Inches(13.333)
prs.slide_height = Inches(7.5)

# --- PALETTE (Strictly from user's template) ---
COLOR_BORDER = RGBColor(190, 222, 225)       # Soft pastel cyan/teal border
COLOR_WHITE = RGBColor(255, 255, 255)
COLOR_ICE_BG = RGBColor(242, 248, 249)       # Clean ice card
COLOR_MINT_BG = RGBColor(235, 244, 241)      # Subtle mint card
COLOR_TITLE = RGBColor(20, 35, 40)           # Crisp dark charcoal
COLOR_BODY = RGBColor(45, 55, 60)            # Sharp legible body text
COLOR_ACCENT = RGBColor(38, 128, 140)        # Professional teal accent
COLOR_BOX_BORDER = RGBColor(185, 212, 215)   # Sharp 1pt card outline

FONT_TITLE = "Trebuchet MS"
FONT_BODY = "Calibri"

def apply_slide_frame(slide):
    # Outer template border
    bg = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0), Inches(0), Inches(13.333), Inches(7.5))
    bg.fill.solid()
    bg.fill.fore_color.rgb = COLOR_BORDER
    bg.line.color.rgb = COLOR_BORDER

    # Inner crisp white canvas
    inner = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.4), Inches(0.4), Inches(12.533), Inches(6.7))
    inner.fill.solid()
    inner.fill.fore_color.rgb = COLOR_WHITE
    inner.line.fill.background()

def add_header(slide, title_text, category_text=None):
    apply_slide_frame(slide)

    header_box = slide.shapes.add_textbox(Inches(0.8), Inches(0.65), Inches(11.7), Inches(1.1))
    tf = header_box.text_frame
    tf.word_wrap = True
    tf.margin_left = tf.margin_top = tf.margin_right = tf.margin_bottom = 0

    if category_text:
        p_cat = tf.paragraphs[0]
        p_cat.text = category_text.upper()
        p_cat.font.name = FONT_BODY
        p_cat.font.size = Pt(11)
        p_cat.font.bold = True
        p_cat.font.color.rgb = COLOR_ACCENT
        p_cat.space_after = Pt(3)
        p_title = tf.add_paragraph()
    else:
        p_title = tf.paragraphs[0]

    p_title.text = title_text
    p_title.font.name = FONT_TITLE
    p_title.font.size = Pt(23)
    p_title.font.bold = True
    p_title.font.color.rgb = COLOR_TITLE

    # Small sharp teal accent line
    line = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.8), Inches(1.75), Inches(1.2), Inches(0.04))
    line.fill.solid()
    line.fill.fore_color.rgb = COLOR_ACCENT
    line.line.color.rgb = COLOR_ACCENT

def add_image_box(slide, img_path, left, top, width, height):
    """Adds a picture with a sharp 1.5pt border."""
    if os.path.exists(img_path):
        pic = slide.shapes.add_picture(img_path, left, top, width, height)
        border = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, left, top, width, height)
        border.fill.background()
        border.line.color.rgb = COLOR_BOX_BORDER
        border.line.width = Pt(1.5)

# ==========================================================
# SLIDE 1: Title
# ==========================================================
slide1 = prs.slides.add_slide(prs.slide_layouts[6])
apply_slide_frame(slide1)

t_box = slide1.shapes.add_textbox(Inches(1.2), Inches(2.0), Inches(10.9), Inches(3.6))
tf1 = t_box.text_frame
tf1.word_wrap = True
tf1.margin_left = tf1.margin_top = tf1.margin_right = tf1.margin_bottom = 0

p1 = tf1.paragraphs[0]
p1.text = "AI in Breast Cancer Screening: The MASAI Case Study"
p1.font.name = FONT_TITLE
p1.font.size = Pt(34)
p1.font.bold = True
p1.alignment = PP_ALIGN.CENTER
p1.font.color.rgb = COLOR_TITLE
p1.space_after = Pt(10)

p2 = tf1.add_paragraph()
p2.text = "Helping Doctors Read Scans Without Replacing Them"
p2.font.name = FONT_BODY
p2.font.size = Pt(20)
p2.font.bold = True
p2.font.color.rgb = COLOR_ACCENT
p2.alignment = PP_ALIGN.CENTER
p2.space_after = Pt(24)

# Center accent line
accent = slide1.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(5.9), Inches(3.9), Inches(1.5), Inches(0.04))
accent.fill.solid()
accent.fill.fore_color.rgb = COLOR_ACCENT
accent.line.color.rgb = COLOR_ACCENT

p3 = tf1.add_paragraph()
p3.text = "\nCase: Sweden's AI Mammography Screening Trial\nPresenter: [Your Name] | 3rd Year B.E. Computer Science"
p3.font.name = FONT_BODY
p3.font.size = Pt(14)
p3.font.color.rgb = COLOR_BODY
p3.alignment = PP_ALIGN.CENTER

# ==========================================================
# SLIDE 2: Introduction – Overview of the Subject
# Layout: Content Left, Image Right
# ==========================================================
slide2 = prs.slides.add_slide(prs.slide_layouts[6])
add_header(slide2, "Introduction – Overview of the Subject", "Case Study")

# Left sharp content box
c2 = slide2.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.8), Inches(2.1), Inches(6.8), Inches(4.6))
c2.fill.solid()
c2.fill.fore_color.rgb = COLOR_ICE_BG
c2.line.color.rgb = COLOR_BOX_BORDER
c2.line.width = Pt(1)

tf2 = c2.text_frame
tf2.word_wrap = True
tf2.margin_left = tf2.margin_right = Inches(0.4)
tf2.margin_top = Inches(0.35)

p = tf2.paragraphs[0]
p.text = "What the system is for:"
p.font.name = FONT_TITLE
p.font.size = Pt(18)
p.font.bold = True
p.font.color.rgb = COLOR_TITLE
p.space_after = Pt(8)

p = tf2.add_paragraph()
p.text = "It is an AI tool that automatically scans mammogram X-rays, highlights suspicious spots, and flags potential cancer early so doctors don’t miss it."
p.font.name = FONT_BODY
p.font.size = Pt(15)
p.font.color.rgb = COLOR_BODY
p.space_after = Pt(28)

p = tf2.add_paragraph()
p.text = "Why it is a cognitive system:"
p.font.name = FONT_TITLE
p.font.size = Pt(18)
p.font.bold = True
p.font.color.rgb = COLOR_TITLE
p.space_after = Pt(8)

p = tf2.add_paragraph()
p.text = "It is designed to assist and collaborate with human doctors—augmenting their judgment rather than trying to replace them."
p.font.name = FONT_BODY
p.font.size = Pt(15)
p.font.color.rgb = COLOR_BODY

# Right Image: Mammogram / X-ray scan
add_image_box(slide2, "d:/java project/images/xray_scan.jpg", Inches(7.9), Inches(2.1), Inches(4.6), Inches(4.6))

# ==========================================================
# SLIDE 3: Background Information – How Screening Worked Before AI
# Layout: Content Left, Image Right
# ==========================================================
slide3 = prs.slides.add_slide(prs.slide_layouts[6])
add_header(slide3, "Background Information – How Screening Worked Before AI", "Historical Context")

c3 = slide3.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.8), Inches(2.1), Inches(6.8), Inches(4.6))
c3.fill.solid()
c3.fill.fore_color.rgb = COLOR_ICE_BG
c3.line.color.rgb = COLOR_BOX_BORDER
c3.line.width = Pt(1)

tf3 = c3.text_frame
tf3.word_wrap = True
tf3.margin_left = tf3.margin_right = Inches(0.4)
tf3.margin_top = Inches(0.35)

p = tf3.paragraphs[0]
p.text = "The Old Routine:"
p.font.name = FONT_TITLE
p.font.size = Pt(16)
p.font.bold = True
p.font.color.rgb = COLOR_TITLE
p.space_after = Pt(4)

p = tf3.add_paragraph()
p.text = "In Sweden, about 1 million women are screened every year, and by law, every single scan had to be looked at by two separate doctors to avoid mistakes."
p.font.name = FONT_BODY
p.font.size = Pt(14)
p.font.color.rgb = COLOR_BODY
p.space_after = Pt(18)

p = tf3.add_paragraph()
p.text = "The Human Problem:"
p.font.name = FONT_TITLE
p.font.size = Pt(16)
p.font.bold = True
p.font.color.rgb = COLOR_TITLE
p.space_after = Pt(4)

p = tf3.add_paragraph()
p.text = "Doctors have to read roughly 50 scans an hour in dark rooms, which causes massive eye strain, fatigue, and missed details."
p.font.name = FONT_BODY
p.font.size = Pt(14)
p.font.color.rgb = COLOR_BODY
p.space_after = Pt(18)

p = tf3.add_paragraph()
p.text = "Doctor Shortage:"
p.font.name = FONT_TITLE
p.font.size = Pt(16)
p.font.bold = True
p.font.color.rgb = COLOR_TITLE
p.space_after = Pt(4)

p = tf3.add_paragraph()
p.text = "There is a severe worldwide shortage of trained breast radiologists to handle this growing workload."
p.font.name = FONT_BODY
p.font.size = Pt(14)
p.font.color.rgb = COLOR_BODY

# Right Image: Doctor reading scans
add_image_box(slide3, "d:/java project/images/doctor_reading.jpg", Inches(7.9), Inches(2.1), Inches(4.6), Inches(4.6))

# ==========================================================
# SLIDE 4: Problem Statement – Challenges Faced
# Layout: Two sharp boxes side-by-side
# ==========================================================
slide4 = prs.slides.add_slide(prs.slide_layouts[6])
add_header(slide4, "Problem Statement – Challenges Faced", "Core Dilemmas")

# Left Box: Global Health Burden
b4_l = slide4.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.8), Inches(2.1), Inches(5.6), Inches(4.6))
b4_l.fill.solid()
b4_l.fill.fore_color.rgb = COLOR_ICE_BG
b4_l.line.color.rgb = COLOR_BOX_BORDER
b4_l.line.width = Pt(1)

tf4_l = b4_l.text_frame
tf4_l.word_wrap = True
tf4_l.margin_left = tf4_l.margin_right = Inches(0.4)
tf4_l.margin_top = Inches(0.35)

p = tf4_l.paragraphs[0]
p.text = "Global Health Burden (WHO / IARC):"
p.font.name = FONT_TITLE
p.font.size = Pt(17)
p.font.bold = True
p.font.color.rgb = COLOR_TITLE
p.space_after = Pt(14)

p = tf4_l.add_paragraph()
p.text = "• 2.3 Million new cases & 670,000 deaths per year (#1 cancer in women; 1 in 12 lifetime risk)"
p.font.name = FONT_BODY
p.font.size = Pt(14.5)
p.font.color.rgb = COLOR_BODY
p.space_after = Pt(16)

p = tf4_l.add_paragraph()
p.text = "• Projected by 2050: Cases +38% (~3.2M) and Deaths +68% (>1.1M), completely outstripping hospital staffing"
p.font.name = FONT_BODY
p.font.size = Pt(14.5)
p.font.color.rgb = COLOR_BODY

# Right Box: The Three Screening Dilemmas
b4_r = slide4.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(6.8), Inches(2.1), Inches(5.7), Inches(4.6))
b4_r.fill.solid()
b4_r.fill.fore_color.rgb = COLOR_MINT_BG
b4_r.line.color.rgb = COLOR_BOX_BORDER
b4_r.line.width = Pt(1)

tf4_r = b4_r.text_frame
tf4_r.word_wrap = True
tf4_r.margin_left = tf4_r.margin_right = Inches(0.4)
tf4_r.margin_top = Inches(0.35)

p = tf4_r.paragraphs[0]
p.text = "The Three Screening Dilemmas:"
p.font.name = FONT_TITLE
p.font.size = Pt(17)
p.font.bold = True
p.font.color.rgb = COLOR_TITLE
p.space_after = Pt(14)

p = tf4_r.add_paragraph()
p.text = "• False Positives:\nCalling healthy women back for terrifying rechecks and unneeded biopsies"
p.font.name = FONT_BODY
p.font.size = Pt(14)
p.font.color.rgb = COLOR_BODY
p.space_after = Pt(12)

p = tf4_r.add_paragraph()
p.text = "• Overdiagnosis:\nCatching tiny, harmless lumps that lead to unnecessary surgery and treatments"
p.font.name = FONT_BODY
p.font.size = Pt(14)
p.font.color.rgb = COLOR_BODY
p.space_after = Pt(12)

p = tf4_r.add_paragraph()
p.text = "• Interval Cancers:\nDeadly, fast-growing cancers missed during screening that surface between appointments"
p.font.name = FONT_BODY
p.font.size = Pt(14)
p.font.color.rgb = COLOR_BODY

# ==========================================================
# SLIDE 5: Solutions – Deploying the AI System
# Layout: Content Left, Image Right
# ==========================================================
slide5 = prs.slides.add_slide(prs.slide_layouts[6])
add_header(slide5, "Solutions – Deploying the AI System", "System Architecture")

c5 = slide5.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.8), Inches(2.1), Inches(6.8), Inches(4.6))
c5.fill.solid()
c5.fill.fore_color.rgb = COLOR_ICE_BG
c5.line.color.rgb = COLOR_BOX_BORDER
c5.line.width = Pt(1)

tf5 = c5.text_frame
tf5.word_wrap = True
tf5.margin_left = tf5.margin_right = Inches(0.4)
tf5.margin_top = Inches(0.4)

p = tf5.paragraphs[0]
p.text = "The Solution:"
p.font.name = FONT_TITLE
p.font.size = Pt(17)
p.font.bold = True
p.font.color.rgb = COLOR_TITLE
p.space_after = Pt(6)

p = tf5.add_paragraph()
p.text = "An AI software called Transpara was deployed to act as an extra pair of eyes for radiologists."
p.font.name = FONT_BODY
p.font.size = Pt(15)
p.font.color.rgb = COLOR_BODY
p.space_after = Pt(28)

p = tf5.add_paragraph()
p.text = "Trained on Historical Data:"
p.font.name = FONT_TITLE
p.font.size = Pt(17)
p.font.bold = True
p.font.color.rgb = COLOR_TITLE
p.space_after = Pt(6)

p = tf5.add_paragraph()
p.text = "The AI was trained on hundreds of thousands of past mammograms with verified biopsy results, teaching it to recognize what real cancer looks like."
p.font.name = FONT_BODY
p.font.size = Pt(15)
p.font.color.rgb = COLOR_BODY

# Right Image: AI technology in healthcare
add_image_box(slide5, "d:/java project/images/tech_health.jpg", Inches(7.9), Inches(2.1), Inches(4.6), Inches(4.6))

# ==========================================================
# SLIDE 6: Implementation – How It Works in Practice
# Layout: 3 sharp-edged columns
# ==========================================================
slide6 = prs.slides.add_slide(prs.slide_layouts[6])
add_header(slide6, "Implementation – How It Works in Practice", "Clinical Workflow")

# Col 1: Risk Score
b6_1 = slide6.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.8), Inches(2.1), Inches(3.6), Inches(4.6))
b6_1.fill.solid()
b6_1.fill.fore_color.rgb = COLOR_ICE_BG
b6_1.line.color.rgb = COLOR_BOX_BORDER
b6_1.line.width = Pt(1)

tf6_1 = b6_1.text_frame
tf6_1.word_wrap = True
tf6_1.margin_left = tf6_1.margin_right = Inches(0.3)
tf6_1.margin_top = Inches(0.35)

p = tf6_1.paragraphs[0]
p.text = "The 1 to 10 Risk Score:"
p.font.name = FONT_TITLE
p.font.size = Pt(16)
p.font.bold = True
p.font.color.rgb = COLOR_TITLE
p.space_after = Pt(10)

p = tf6_1.add_paragraph()
p.text = "The AI checks each scan, marks suspicious spots, and rates cancer risk from 1 (lowest risk) to 10 (highest risk)."
p.font.name = FONT_BODY
p.font.size = Pt(14.5)
p.font.color.rgb = COLOR_BODY

# Col 2: How Doctors Use It
b6_2 = slide6.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(4.8), Inches(2.1), Inches(4.1), Inches(4.6))
b6_2.fill.solid()
b6_2.fill.fore_color.rgb = COLOR_MINT_BG
b6_2.line.color.rgb = COLOR_BOX_BORDER
b6_2.line.width = Pt(1)

tf6_2 = b6_2.text_frame
tf6_2.word_wrap = True
tf6_2.margin_left = tf6_2.margin_right = Inches(0.3)
tf6_2.margin_top = Inches(0.35)

p = tf6_2.paragraphs[0]
p.text = "How Doctors Use It:"
p.font.name = FONT_TITLE
p.font.size = Pt(16)
p.font.bold = True
p.font.color.rgb = COLOR_TITLE
p.space_after = Pt(10)

p = tf6_2.add_paragraph()
p.text = "• Scores 1 to 9 (Normal / Low Risk):\nOnly 1 doctor reads the scan, using the AI's marks."
p.font.name = FONT_BODY
p.font.size = Pt(14)
p.font.color.rgb = COLOR_BODY
p.space_after = Pt(14)

p = tf6_2.add_paragraph()
p.text = "• Score 10 (Highest Risk):\nKept as double reading by 2 doctors."
p.font.name = FONT_BODY
p.font.size = Pt(14)
p.font.color.rgb = COLOR_BODY

# Col 3: Doctor Has Final Say
b6_3 = slide6.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(9.3), Inches(2.1), Inches(3.2), Inches(4.6))
b6_3.fill.solid()
b6_3.fill.fore_color.rgb = COLOR_ICE_BG
b6_3.line.color.rgb = COLOR_BOX_BORDER
b6_3.line.width = Pt(1)

tf6_3 = b6_3.text_frame
tf6_3.word_wrap = True
tf6_3.margin_left = tf6_3.margin_right = Inches(0.3)
tf6_3.margin_top = Inches(0.35)

p = tf6_3.paragraphs[0]
p.text = "Doctor Has Final Say:"
p.font.name = FONT_TITLE
p.font.size = Pt(16)
p.font.bold = True
p.font.color.rgb = COLOR_TITLE
p.space_after = Pt(10)

p = tf6_3.add_paragraph()
p.text = "The AI cannot call a patient back on its own; the human doctor always makes the final decision."
p.font.name = FONT_BODY
p.font.size = Pt(14.5)
p.font.color.rgb = COLOR_BODY

# ==========================================================
# SLIDE 7: Case Analysis – Testing the System
# Layout: Content Left, Image Right
# ==========================================================
slide7 = prs.slides.add_slide(prs.slide_layouts[6])
add_header(slide7, "Case Analysis – Testing the System", "The Trial")

c7 = slide7.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.8), Inches(2.1), Inches(6.8), Inches(4.6))
c7.fill.solid()
c7.fill.fore_color.rgb = COLOR_ICE_BG
c7.line.color.rgb = COLOR_BOX_BORDER
c7.line.width = Pt(1)

tf7 = c7.text_frame
tf7.word_wrap = True
tf7.margin_left = tf7.margin_right = Inches(0.4)
tf7.margin_top = Inches(0.35)

p = tf7.paragraphs[0]
p.text = "The Test Setup:"
p.font.name = FONT_TITLE
p.font.size = Pt(16)
p.font.bold = True
p.font.color.rgb = COLOR_TITLE
p.space_after = Pt(4)

p = tf7.add_paragraph()
p.text = "Over 100,000 women in Sweden were tested across 4 screening centers in a live clinical trial."
p.font.name = FONT_BODY
p.font.size = Pt(14)
p.font.color.rgb = COLOR_BODY
p.space_after = Pt(16)

p = tf7.add_paragraph()
p.text = "The Comparison:"
p.font.name = FONT_TITLE
p.font.size = Pt(16)
p.font.bold = True
p.font.color.rgb = COLOR_TITLE
p.space_after = Pt(4)

p = tf7.add_paragraph()
p.text = "Half the women had their scans checked the old way by two doctors; the other half had their scans checked using the new AI triage system."
p.font.name = FONT_BODY
p.font.size = Pt(14)
p.font.color.rgb = COLOR_BODY
p.space_after = Pt(16)

p = tf7.add_paragraph()
p.text = "The Goal:"
p.font.name = FONT_TITLE
p.font.size = Pt(16)
p.font.bold = True
p.font.color.rgb = COLOR_TITLE
p.space_after = Pt(4)

p = tf7.add_paragraph()
p.text = "To test with real patient data whether letting AI handle some of the workload was truly safe."
p.font.name = FONT_BODY
p.font.size = Pt(14)
p.font.color.rgb = COLOR_BODY

# Right Image: Hospital clinical setting
add_image_box(slide7, "d:/java project/images/hospital_clinic.jpg", Inches(7.9), Inches(2.1), Inches(4.6), Inches(4.6))

# ==========================================================
# SLIDE 8: Results and Outcomes – What Happened
# Layout: 4 sharp rectangular cards (2x2 grid)
# ==========================================================
slide8 = prs.slides.add_slide(prs.slide_layouts[6])
add_header(slide8, "Results and Outcomes – What Happened", "Proven Impact")

cards8_data = [
    ("Workload Cut by 44%:", "Doctor reading time dropped by almost half, saving thousands of hospital hours.", COLOR_ICE_BG),
    ("Found 29% More Cancers:", "Caught significantly more early-stage, dangerous cancers than two human doctors.", COLOR_MINT_BG),
    ("Fewer Missed Cancers:", "Missed cancers that appear between regular checkups dropped by 12%.", COLOR_MINT_BG),
    ("No Extra False Alarms:", "Accuracy stayed rock-solid at 98.5%, meaning healthy patients were not panicked.", COLOR_ICE_BG)
]

coords8 = [
    (Inches(0.8), Inches(2.1)),
    (Inches(6.8), Inches(2.1)),
    (Inches(0.8), Inches(4.5)),
    (Inches(6.8), Inches(4.5))
]

for (heading, body, bg_col), (x, y) in zip(cards8_data, coords8):
    box = slide8.shapes.add_shape(MSO_SHAPE.RECTANGLE, x, y, Inches(5.7), Inches(2.1))
    box.fill.solid()
    box.fill.fore_color.rgb = bg_col
    box.line.color.rgb = COLOR_BOX_BORDER
    box.line.width = Pt(1)

    tf = box.text_frame
    tf.word_wrap = True
    tf.margin_left = tf.margin_right = Inches(0.35)
    tf.margin_top = Inches(0.25)

    p1 = tf.paragraphs[0]
    p1.text = heading
    p1.font.name = FONT_TITLE
    p1.font.size = Pt(16)
    p1.font.bold = True
    p1.font.color.rgb = COLOR_ACCENT
    p1.space_after = Pt(6)

    p2 = tf.add_paragraph()
    p2.text = body
    p2.font.name = FONT_BODY
    p2.font.size = Pt(14)
    p2.font.color.rgb = COLOR_BODY

# ==========================================================
# SLIDE 9: Conclusions and Recommendations – Future Directions
# Layout: Content Left, Image Right
# ==========================================================
slide9 = prs.slides.add_slide(prs.slide_layouts[6])
add_header(slide9, "Conclusions and Recommendations – Future Directions", "Looking Forward")

c9 = slide9.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.8), Inches(2.1), Inches(6.8), Inches(4.6))
c9.fill.solid()
c9.fill.fore_color.rgb = COLOR_ICE_BG
c9.line.color.rgb = COLOR_BOX_BORDER
c9.line.width = Pt(1)

tf9 = c9.text_frame
tf9.word_wrap = True
tf9.margin_left = tf9.margin_right = Inches(0.4)
tf9.margin_top = Inches(0.35)

p = tf9.paragraphs[0]
p.text = "Main Takeaway:"
p.font.name = FONT_TITLE
p.font.size = Pt(16)
p.font.bold = True
p.font.color.rgb = COLOR_TITLE
p.space_after = Pt(4)

p = tf9.add_paragraph()
p.text = "Cognitive systems work best when assisting humans rather than trying to replace them."
p.font.name = FONT_BODY
p.font.size = Pt(14)
p.font.color.rgb = COLOR_BODY
p.space_after = Pt(18)

p = tf9.add_paragraph()
p.text = "Expanding to Developing Nations:"
p.font.name = FONT_TITLE
p.font.size = Pt(16)
p.font.bold = True
p.font.color.rgb = COLOR_TITLE
p.space_after = Pt(4)

p = tf9.add_paragraph()
p.text = "The trial is now expanding to countries like Ethiopia, where there is an extreme shortage of radiologists."
p.font.name = FONT_BODY
p.font.size = Pt(14)
p.font.color.rgb = COLOR_BODY
p.space_after = Pt(18)

p = tf9.add_paragraph()
p.text = "Next Steps:"
p.font.name = FONT_TITLE
p.font.size = Pt(16)
p.font.bold = True
p.font.color.rgb = COLOR_TITLE
p.space_after = Pt(4)

p = tf9.add_paragraph()
p.text = "Testing the software across different scanner brands and diverse populations."
p.font.name = FONT_BODY
p.font.size = Pt(14)
p.font.color.rgb = COLOR_BODY

# Right Image: Global healthcare
add_image_box(slide9, "d:/java project/images/global_care.jpg", Inches(7.9), Inches(2.1), Inches(4.6), Inches(4.6))

# ==========================================================
# SLIDE 10: References
# Layout: Clean full-width reference card
# ==========================================================
slide10 = prs.slides.add_slide(prs.slide_layouts[6])
add_header(slide10, "References", "APA 7th Edition")

c10 = slide10.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.8), Inches(2.1), Inches(11.7), Inches(4.6))
c10.fill.solid()
c10.fill.fore_color.rgb = COLOR_ICE_BG
c10.line.color.rgb = COLOR_BOX_BORDER
c10.line.width = Pt(1)

tf10 = c10.text_frame
tf10.word_wrap = True
tf10.margin_left = tf10.margin_right = Inches(0.4)
tf10.margin_top = Inches(0.3)

p = tf10.paragraphs[0]
p.text = "(Unspoken — leave on screen during teacher Q&A)"
p.font.name = FONT_BODY
p.font.size = Pt(12)
p.font.italic = True
p.font.color.rgb = COLOR_ACCENT
p.space_after = Pt(12)

ref_list = [
    "Gommers, J., et al. (2026). Interval cancer, sensitivity, and specificity comparing AI-supported mammography screening with standard double reading in the MASAI study. The Lancet, 407(10527), 505–514.",
    "Hernström, V., et al. (2025). Screening performance and characteristics of breast cancer detected in the MASAI trial. The Lancet Digital Health, 7(3), e175–e183.",
    "Lång, K., et al. (2023). Artificial intelligence-supported screen reading versus standard double reading in the MASAI trial. The Lancet Oncology, 24(8), 936–944.",
    "International Agency for Research on Cancer (IARC / WHO). (2025). Global Breast Cancer Burden: Trends and Projections to 2050.",
    "Tornatzky, L. G., & Fleischer, M. (1990). The Processes of Technological Innovation."
]

for r in ref_list:
    p = tf10.add_paragraph()
    p.text = r
    p.font.name = FONT_BODY
    p.font.size = Pt(13)
    p.font.color.rgb = COLOR_BODY
    p.space_after = Pt(10)

# Save
prs.save(r"d:\java project\MASAI_Trial_Case_Study.pptx")
print("Presentation regenerated successfully with EXACT agreed text and themed images!")
