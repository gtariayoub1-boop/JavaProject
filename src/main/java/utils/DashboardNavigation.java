package utils;

public final class DashboardNavigation {

    public enum TeacherSection {
        PROFILE,
        STUDENTS,
        CHANGE_PASSWORD
    }

    public enum StudentSection {
        PROFILE,
        CHANGE_PASSWORD
    }

    private static TeacherSection teacherSection = TeacherSection.PROFILE;
    private static StudentSection studentSection = StudentSection.PROFILE;

    private DashboardNavigation() {
    }

    public static void openTeacherSection(TeacherSection section) {
        teacherSection = section != null ? section : TeacherSection.PROFILE;
    }

    public static TeacherSection consumeTeacherSection() {
        TeacherSection section = teacherSection;
        teacherSection = TeacherSection.PROFILE;
        return section;
    }

    public static void openStudentSection(StudentSection section) {
        studentSection = section != null ? section : StudentSection.PROFILE;
    }

    public static StudentSection consumeStudentSection() {
        StudentSection section = studentSection;
        studentSection = StudentSection.PROFILE;
        return section;
    }
}
