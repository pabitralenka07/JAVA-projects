package com.portfolio.resume.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.portfolio.resume.model.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

@Service
public class PdfResumeService {

    // Harmonious modern color tokens
    private static final Color COLOR_PRIMARY = new Color(15, 23, 42);       // #0f172a
    private static final Color COLOR_ACCENT_TECH = new Color(124, 58, 237); // #7c3aed
    private static final Color COLOR_ACCENT_EXEC = new Color(71, 85, 105);  // #475569
    private static final Color COLOR_BODY = new Color(51, 65, 85);          // #334155
    private static final Color COLOR_MUTED = new Color(100, 116, 139);      // #64748b

    public byte[] generatePdfResume(Resume resume) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        generatePdfResume(resume, baos);
        return baos.toByteArray();
    }

    public void generatePdfResume(Resume resume, OutputStream out) {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, out);
            document.addTitle(resume.getFullName() + " - Resume");
            document.addAuthor(resume.getFullName());
            document.addSubject("Curriculum Vitae / Resume generated via ResumeSphere");
            document.addCreator("ResumeSphere Online Resume Builder");

            document.open();

            boolean isExec = (resume.getTemplate() == ResumeTemplate.EXECUTIVE);
            Color accentColor = isExec ? COLOR_ACCENT_EXEC : COLOR_ACCENT_TECH;

            // 1. Header Section
            renderHeader(document, resume, isExec, accentColor);

            // 2. Summary
            if (resume.getSummary() != null && !resume.getSummary().isBlank()) {
                addSectionHeading(document, "PROFESSIONAL SUMMARY", accentColor);
                Paragraph summaryPara = new Paragraph(resume.getSummary().trim(),
                        FontFactory.getFont(FontFactory.HELVETICA, 9.5f, Font.NORMAL, COLOR_BODY));
                summaryPara.setSpacingAfter(10f);
                summaryPara.setLeading(13.5f);
                document.add(summaryPara);
            }

            // 3. Technical Skills
            if (!resume.getSkillCategoryList().isEmpty()) {
                addSectionHeading(document, "TECHNICAL SKILLS", accentColor);
                for (SkillCategory cat : resume.getSkillCategoryList()) {
                    Paragraph skillPara = new Paragraph();
                    skillPara.setLeading(13f);
                    Chunk titleChunk = new Chunk(cat.getCategoryName() + ": ",
                            FontFactory.getFont(FontFactory.HELVETICA, 9.5f, Font.BOLD, COLOR_PRIMARY));
                    Chunk bodyChunk = new Chunk(cat.getSkillsCsv(),
                            FontFactory.getFont(FontFactory.HELVETICA, 9.5f, Font.NORMAL, COLOR_BODY));
                    skillPara.add(titleChunk);
                    skillPara.add(bodyChunk);
                    document.add(skillPara);
                }
                addSpacing(document, 8f);
            }

            // 4. Experience / Work History
            if (!resume.getExperienceList().isEmpty()) {
                addSectionHeading(document, "WORK EXPERIENCE", accentColor);
                for (ExperienceItem exp : resume.getExperienceList()) {
                    PdfPTable expTable = new PdfPTable(2);
                    expTable.setWidthPercentage(100);
                    expTable.setWidths(new float[]{70, 30});

                    // Role & Company
                    Paragraph left = new Paragraph();
                    left.setLeading(12f);
                    left.add(new Chunk(exp.getRole(), FontFactory.getFont(FontFactory.HELVETICA, 10f, Font.BOLD, COLOR_PRIMARY)));
                    left.add(new Chunk(" | " + exp.getCompany(), FontFactory.getFont(FontFactory.HELVETICA, 10f, Font.NORMAL, COLOR_BODY)));

                    // Dates & Location
                    Paragraph right = new Paragraph();
                    right.setAlignment(Element.ALIGN_RIGHT);
                    right.setLeading(12f);
                    String dateRange = (exp.getStartDate() != null ? exp.getStartDate() : "") +
                            " - " + (exp.isCurrentRole() ? "Present" : (exp.getEndDate() != null ? exp.getEndDate() : ""));
                    right.add(new Chunk(dateRange, FontFactory.getFont(FontFactory.HELVETICA, 9f, Font.ITALIC, COLOR_MUTED)));
                    if (exp.getLocation() != null && !exp.getLocation().isBlank()) {
                        right.add(new Chunk(" (" + exp.getLocation() + ")", FontFactory.getFont(FontFactory.HELVETICA, 9f, Font.NORMAL, COLOR_MUTED)));
                    }

                    PdfPCell c1 = createCleanCell(left);
                    PdfPCell c2 = createCleanCell(right);
                    c2.setHorizontalAlignment(Element.ALIGN_RIGHT);

                    expTable.addCell(c1);
                    expTable.addCell(c2);
                    document.add(expTable);

                    // Bullet points
                    com.lowagie.text.List list = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED, 12);
                    list.setListSymbol("• ");
                    for (String bp : exp.getBulletPointList()) {
                        ListItem item = new ListItem(bp, FontFactory.getFont(FontFactory.HELVETICA, 9f, Font.NORMAL, COLOR_BODY));
                        item.setLeading(12.5f);
                        list.add(item);
                    }
                    document.add(list);
                    addSpacing(document, 6f);
                }
            }

            // 5. Projects
            if (!resume.getProjectList().isEmpty()) {
                addSectionHeading(document, "KEY PROJECTS", accentColor);
                for (ProjectItem proj : resume.getProjectList()) {
                    PdfPTable projTable = new PdfPTable(2);
                    projTable.setWidthPercentage(100);
                    projTable.setWidths(new float[]{75, 25});

                    Paragraph left = new Paragraph();
                    left.setLeading(12f);
                    left.add(new Chunk(proj.getTitle(), FontFactory.getFont(FontFactory.HELVETICA, 10f, Font.BOLD, COLOR_PRIMARY)));
                    if (proj.getTechnologies() != null && !proj.getTechnologies().isBlank()) {
                        left.add(new Chunk(" (" + proj.getTechnologies() + ")", FontFactory.getFont(FontFactory.HELVETICA, 9f, Font.ITALIC, accentColor)));
                    }

                    Paragraph right = new Paragraph();
                    right.setAlignment(Element.ALIGN_RIGHT);
                    right.setLeading(12f);
                    if (proj.getProjectUrl() != null && !proj.getProjectUrl().isBlank()) {
                        right.add(new Chunk(proj.getProjectUrl(), FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Font.UNDERLINE, COLOR_MUTED)));
                    }

                    PdfPCell c1 = createCleanCell(left);
                    PdfPCell c2 = createCleanCell(right);
                    c2.setHorizontalAlignment(Element.ALIGN_RIGHT);

                    projTable.addCell(c1);
                    projTable.addCell(c2);
                    document.add(projTable);

                    if (proj.getDescription() != null && !proj.getDescription().isBlank()) {
                        Paragraph desc = new Paragraph(proj.getDescription().trim(),
                                FontFactory.getFont(FontFactory.HELVETICA, 9f, Font.NORMAL, COLOR_BODY));
                        desc.setLeading(12.5f);
                        desc.setSpacingAfter(6f);
                        document.add(desc);
                    }
                }
            }

            // 6. Education
            if (!resume.getEducationList().isEmpty()) {
                addSectionHeading(document, "EDUCATION", accentColor);
                for (EducationItem edu : resume.getEducationList()) {
                    PdfPTable eduTable = new PdfPTable(2);
                    eduTable.setWidthPercentage(100);
                    eduTable.setWidths(new float[]{70, 30});

                    Paragraph left = new Paragraph();
                    left.setLeading(12f);
                    String degField = edu.getDegree() + (edu.getFieldOfStudy() != null && !edu.getFieldOfStudy().isBlank() ? " in " + edu.getFieldOfStudy() : "");
                    left.add(new Chunk(degField, FontFactory.getFont(FontFactory.HELVETICA, 10f, Font.BOLD, COLOR_PRIMARY)));
                    left.add(new Chunk(" — " + edu.getInstitution(), FontFactory.getFont(FontFactory.HELVETICA, 9.5f, Font.NORMAL, COLOR_BODY)));

                    Paragraph right = new Paragraph();
                    right.setAlignment(Element.ALIGN_RIGHT);
                    right.setLeading(12f);
                    String dates = (edu.getStartDate() != null ? edu.getStartDate() : "") + " - " + (edu.getEndDate() != null ? edu.getEndDate() : "");
                    right.add(new Chunk(dates, FontFactory.getFont(FontFactory.HELVETICA, 9f, Font.ITALIC, COLOR_MUTED)));
                    if (edu.getGrade() != null && !edu.getGrade().isBlank()) {
                        right.add(new Chunk(" | " + edu.getGrade(), FontFactory.getFont(FontFactory.HELVETICA, 9f, Font.BOLD, COLOR_PRIMARY)));
                    }

                    PdfPCell c1 = createCleanCell(left);
                    PdfPCell c2 = createCleanCell(right);
                    c2.setHorizontalAlignment(Element.ALIGN_RIGHT);

                    eduTable.addCell(c1);
                    eduTable.addCell(c2);
                    document.add(eduTable);
                    addSpacing(document, 4f);
                }
            }

            // 7. Certifications
            if (!resume.getCertificationList().isEmpty()) {
                addSectionHeading(document, "CERTIFICATIONS & ACHIEVEMENTS", accentColor);
                for (CertificationItem cert : resume.getCertificationList()) {
                    Paragraph certPara = new Paragraph();
                    certPara.setLeading(12.5f);
                    certPara.add(new Chunk(cert.getName(), FontFactory.getFont(FontFactory.HELVETICA, 9.5f, Font.BOLD, COLOR_PRIMARY)));
                    if (cert.getIssuer() != null && !cert.getIssuer().isBlank()) {
                        certPara.add(new Chunk(" — " + cert.getIssuer(), FontFactory.getFont(FontFactory.HELVETICA, 9f, Font.NORMAL, COLOR_BODY)));
                    }
                    if (cert.getIssueYear() != null && !cert.getIssueYear().isBlank()) {
                        certPara.add(new Chunk(" (" + cert.getIssueYear() + ")", FontFactory.getFont(FontFactory.HELVETICA, 9f, Font.ITALIC, COLOR_MUTED)));
                    }
                    document.add(certPara);
                }
            }

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate PDF resume: " + e.getMessage(), e);
        }
    }

    private void renderHeader(Document document, Resume resume, boolean isExec, Color accentColor) throws DocumentException {
        int alignment = isExec ? Element.ALIGN_CENTER : Element.ALIGN_LEFT;

        // Name
        Paragraph name = new Paragraph(resume.getFullName().toUpperCase(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20f, Font.BOLD, COLOR_PRIMARY));
        name.setAlignment(alignment);
        name.setLeading(22f);
        document.add(name);

        // Headline
        if (resume.getHeadline() != null && !resume.getHeadline().isBlank()) {
            Paragraph headline = new Paragraph(resume.getHeadline(),
                    FontFactory.getFont(FontFactory.HELVETICA, 10.5f, Font.BOLD, accentColor));
            headline.setAlignment(alignment);
            headline.setLeading(14f);
            document.add(headline);
        }

        // Contact info bar
        StringBuilder contact = new StringBuilder();
        if (resume.getEmail() != null) contact.append(resume.getEmail());
        if (resume.getPhone() != null && !resume.getPhone().isBlank()) contact.append(" • ").append(resume.getPhone());
        if (resume.getLocation() != null && !resume.getLocation().isBlank()) contact.append(" • ").append(resume.getLocation());
        if (resume.getGithubUrl() != null && !resume.getGithubUrl().isBlank()) contact.append(" • ").append(resume.getGithubUrl());
        if (resume.getLinkedinUrl() != null && !resume.getLinkedinUrl().isBlank()) contact.append(" • ").append(resume.getLinkedinUrl());

        Paragraph contactPara = new Paragraph(contact.toString(),
                FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Font.NORMAL, COLOR_MUTED));
        contactPara.setAlignment(alignment);
        contactPara.setLeading(12f);
        contactPara.setSpacingAfter(10f);
        document.add(contactPara);
    }

    private void addSectionHeading(Document document, String title, Color accentColor) throws DocumentException {
        Paragraph heading = new Paragraph(title,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10.5f, Font.BOLD, accentColor));
        heading.setSpacingBefore(8f);
        heading.setSpacingAfter(2f);
        document.add(heading);

        LineSeparator line = new LineSeparator(1f, 100, accentColor, Element.ALIGN_LEFT, -2);
        document.add(line);
        addSpacing(document, 4f);
    }

    private PdfPCell createCleanCell(Paragraph content) {
        PdfPCell cell = new PdfPCell(content);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(0);
        cell.setPaddingBottom(2f);
        return cell;
    }

    private void addSpacing(Document document, float points) throws DocumentException {
        Paragraph spacer = new Paragraph(" ");
        spacer.setLeading(points);
        document.add(spacer);
    }
}
