import os
from PIL import Image, ImageDraw, ImageFont

def draw_timeline(output_path, milestones):
    width = 1800
    height = 750
    bg_color = (255, 255, 255)
    img = Image.new("RGB", (width, height), color=bg_color)
    draw = ImageDraw.Draw(img)

    # Fonts
    font_bold_path = "C:/Windows/Fonts/arialbd.ttf"
    font_reg_path = "C:/Windows/Fonts/arial.ttf"
    
    font_date = ImageFont.truetype(font_bold_path, 25)
    font_label = ImageFont.truetype(font_reg_path, 22)
    font_vert = ImageFont.truetype(font_bold_path, 26)

    # Palette matching original template exactly
    line_color = (35, 45, 55)
    end_circle_color = (0, 0, 0)
    node_fill = (168, 180, 192) # Soft gray fill matching original image
    node_border = (30, 40, 50)
    text_color = (0, 0, 0)
    date_color = (0, 0, 0)

    # Geometry
    y_line = 375
    x_start = 170
    x_end = 1630

    # Draw main horizontal line
    draw.line([(x_start, y_line), (x_end, y_line)], fill=line_color, width=6)

    # End node circles
    end_r = 24
    draw.ellipse([(x_start - end_r, y_line - end_r), (x_start + end_r, y_line + end_r)], fill=end_circle_color)
    draw.ellipse([(x_end - end_r, y_line - end_r), (x_end + end_r, y_line + end_r)], fill=end_circle_color)

    # Draw rotated text for endpoints
    def draw_rotated_text(text, font, color, angle=90):
        bbox = font.getbbox(text)
        w = bbox[2] - bbox[0] + 20
        h = bbox[3] - bbox[1] + 20
        txt_img = Image.new("RGBA", (w, h), (255, 255, 255, 0))
        d = ImageDraw.Draw(txt_img)
        d.text((10, 5), text, font=font, fill=color)
        rotated = txt_img.rotate(angle, expand=True)
        return rotated

    # Left label: PROJECT START
    rot_start = draw_rotated_text("PROJECT START", font_vert, text_color, 90)
    img.paste(rot_start, (x_start - 75, y_line - rot_start.height // 2), rot_start)

    # Right label: PROJECT END
    rot_end = draw_rotated_text("PROJECT END", font_vert, text_color, 90)
    img.paste(rot_end, (x_end + 35, y_line - rot_end.height // 2), rot_end)

    # 6 Milestone nodes
    x_nodes = [370, 580, 790, 1000, 1210, 1420]
    node_r = 22

    for idx, (m_date, m_label, is_date_top) in enumerate(milestones):
        x = x_nodes[idx]

        # Draw milestone circle
        draw.ellipse([(x - node_r, y_line - node_r), (x + node_r, y_line + node_r)], fill=node_fill, outline=node_border, width=4)

        if is_date_top:
            top_text = m_date
            top_font = font_date
            top_color = date_color

            bot_text = m_label
            bot_font = font_label
            bot_color = text_color
        else:
            top_text = m_label
            top_font = font_label
            top_color = text_color

            bot_text = m_date
            bot_font = font_date
            bot_color = date_color

        # Render top text (centered above node)
        top_lines = top_text.split("\n")
        line_height = 29
        total_top_h = len(top_lines) * line_height
        top_y_start = y_line - node_r - 28 - total_top_h

        for l_idx, line in enumerate(top_lines):
            bbox = top_font.getbbox(line)
            lw = bbox[2] - bbox[0]
            ly = top_y_start + (l_idx * line_height)
            draw.text((x - lw // 2, ly), line, font=top_font, fill=top_color)

        # Render bottom text (centered below node)
        bot_lines = bot_text.split("\n")
        bot_y_start = y_line + node_r + 28

        for l_idx, line in enumerate(bot_lines):
            bbox = bot_font.getbbox(line)
            lw = bbox[2] - bbox[0]
            ly = bot_y_start + (l_idx * line_height)
            draw.text((x - lw // 2, ly), line, font=bot_font, fill=bot_color)

    # Save image
    img.save(output_path, "PNG", quality=95)
    print(f"Generated timeline image: {output_path}")

if __name__ == "__main__":
    # Actual verified project creation dates (September 26 to November 20, 2026)
    actual_project_milestones = [
        ("September 26", "Project Research\nIdeation", False),
        ("October 14", "UI/UX Designing", True),
        ("October 27", "Frontend &\nBackend\ndevelopment", False),
        ("November 4", "GIS & Satellite\nTelemetry\nIntegration", True),
        ("November 14", "Testing And\nProduct\nValidation", False),
        ("November 25", "App\nDeployment", True)
    ]

    out_proj = "geopulse_project_timeline.png"
    draw_timeline(out_proj, actual_project_milestones)
