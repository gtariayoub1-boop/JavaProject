package utils;

public final class QuizNavigation {

    public enum TeacherSection {
        LIST,
        CREATE
    }

    public enum StudentSection {
        LIST,
        RESULTS
    }

    private static TeacherSection teacherSection = TeacherSection.LIST;
    private static StudentSection studentSection = StudentSection.LIST;

    private QuizNavigation() {
    }

    public static void openTeacherSection(TeacherSection section) {
        teacherSection = section != null ? section : TeacherSection.LIST;
    }

    public static TeacherSection consumeTeacherSection() {
        TeacherSection section = teacherSection;
        teacherSection = TeacherSection.LIST;
        return section;
    }

    public static void openStudentSection(StudentSection section) {
        studentSection = section != null ? section : StudentSection.LIST;
    }

    public static StudentSection consumeStudentSection() {
        StudentSection section = studentSection;
        studentSection = StudentSection.LIST;
        return section;
    }
}
