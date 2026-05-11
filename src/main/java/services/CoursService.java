package services;

import entities.Cours;
import entities.Etudiant;
import entities.Module;
import services.UtilisateurService;
import utils.DBConnection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CoursService implements IService<Cours> {

    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    @Override
    public void create(Cours cours) throws SQLException {
        String sql = "INSERT INTO cours (code_cours, titre, description, module_id, niveau, credits, langue, "
                + "date_creation, date_debut, date_fin, statut, image_cours_url, prerequis) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(ps, cours);
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    cours.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void createForTeacher(Cours cours, long teacherId) throws SQLException {
        create(cours);
        ensureTeacherCourseLink(cours.getId(), teacherId);
    }

    @Override
    public void update(Cours cours) throws SQLException {
        requireId(cours.getId(), "update cours");
        String sql = "UPDATE cours SET code_cours = ?, titre = ?, description = ?, module_id = ?, niveau = ?, "
                + "credits = ?, langue = ?, date_creation = ?, date_debut = ?, date_fin = ?, statut = ?, "
                + "image_cours_url = ?, prerequis = ? WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            fillStatement(ps, cours);
            ps.setInt(14, cours.getId());
            ps.executeUpdate();
        }
    }

    public void updateForTeacher(Cours cours, long teacherId) throws SQLException {
        update(cours);
        ensureTeacherCourseLink(cours.getId(), teacherId);
    }

    @Override
    public void delete(int id) throws SQLException {
        ContenuService contenuService = new ContenuService();
        for (var contenu : contenuService.getByCoursId(id)) {
            contenuService.delete(contenu.getId());
        }
        String sql = "DELETE FROM cours WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Cours getById(int id) throws SQLException {
        String sql = "SELECT * FROM cours WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCours(rs, true);
                }
            }
        }
        return null;
    }

    @Override
    public List<Cours> getAll() throws SQLException {
        List<Cours> coursList = new ArrayList<>();
        String sql = "SELECT * FROM cours ORDER BY date_creation DESC, id DESC";

        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                coursList.add(mapCours(rs, false));
            }
        }

        return coursList;
    }

    public List<Cours> getByModuleId(int moduleId) throws SQLException {
        List<Cours> coursList = new ArrayList<>();
        String sql = "SELECT * FROM cours WHERE module_id = ? ORDER BY date_creation DESC, id DESC";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, moduleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    coursList.add(mapCours(rs, false));
                }
            }
        }

        return coursList;
    }

    public List<Cours> getByTeacherId(long teacherId) throws SQLException {
        Connection connection = getConnection();
        if (connection == null) {
            throw new SQLException("Database connection is unavailable.");
        }

        for (RelationDefinition definition : teacherCourseRelations()) {
            if (!tableHasColumns(connection, definition.tableName(), definition.leftColumn(), definition.rightColumn())) {
                continue;
            }

            List<Cours> coursList = new ArrayList<>();
            String sql = "SELECT c.* FROM cours c "
                    + "INNER JOIN " + definition.tableName() + " rel ON c.id = rel." + definition.leftColumn() + " "
                    + "WHERE rel." + definition.rightColumn() + " = ? "
                    + "ORDER BY c.date_creation DESC, c.id DESC";

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setLong(1, teacherId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        coursList.add(mapCours(rs, false));
                    }
                }
            }
            return coursList;
        }

        return new ArrayList<>();
    }

    public List<Cours> getByStudentId(long studentId) throws SQLException {
        Connection connection = getConnection();
        if (connection == null) {
            throw new SQLException("Database connection is unavailable.");
        }

        for (RelationDefinition definition : courseStudentRelations()) {
            if (!tableHasColumns(connection, definition.tableName(), definition.leftColumn(), definition.rightColumn())) {
                continue;
            }

            List<Cours> coursList = new ArrayList<>();
            String sql = "SELECT c.* FROM cours c "
                    + "INNER JOIN " + definition.tableName() + " rel ON c.id = rel." + definition.leftColumn() + " "
                    + "WHERE rel." + definition.rightColumn() + " = ? "
                    + "ORDER BY c.date_creation DESC, c.id DESC";

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setLong(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        coursList.add(mapCours(rs, false));
                    }
                }
            }
            return coursList;
        }

        return new ArrayList<>();
    }

    public List<Cours> getAvailableForStudentId(long studentId) throws SQLException {
        List<Cours> allCourses = getAll();
        List<Cours> enrolledCourses = getByStudentId(studentId);
        List<Integer> enrolledIds = enrolledCourses.stream()
                .map(Cours::getId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        return allCourses.stream()
                .filter(cours -> cours.getId() != null && !enrolledIds.contains(cours.getId()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void enrollStudent(int coursId, long studentId) throws SQLException {
        ensureStudentCourseLink(coursId, studentId);
    }

    public List<Etudiant> getStudentsByCoursId(int coursId) throws SQLException {
        Connection connection = getConnection();
        if (connection == null) {
            throw new SQLException("Database connection is unavailable.");
        }

        UtilisateurService utilisateurService = new UtilisateurService();
        for (RelationDefinition definition : courseStudentRelations()) {
            if (!tableHasColumns(connection, definition.tableName(), definition.leftColumn(), definition.rightColumn())) {
                continue;
            }

            List<Etudiant> etudiants = new ArrayList<>();
            String sql = utilisateurService.baseSelectSql()
                    + " INNER JOIN " + definition.tableName() + " rel ON u.id = rel." + definition.rightColumn()
                    + " WHERE rel." + definition.leftColumn() + " = ?"
                    + " AND u.typeUtilisateur = 'etudiant'"
                    + " ORDER BY u.nom, u.prenom, u.id";

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, coursId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        etudiants.add((Etudiant) utilisateurService.mapUtilisateur(rs));
                    }
                }
            }
            return etudiants;
        }

        return new ArrayList<>();
    }

    private void fillStatement(PreparedStatement ps, Cours cours) throws SQLException {
        ps.setString(1, cours.getCodeCours());
        ps.setString(2, cours.getTitre());
        ps.setString(3, cours.getDescription());

        if (cours.getModule() != null && cours.getModule().getId() != null) {
            ps.setInt(4, cours.getModule().getId());
        } else {
            ps.setNull(4, java.sql.Types.INTEGER);
        }

        ps.setString(5, cours.getNiveau());
        if (cours.getCredits() != null) {
            ps.setInt(6, cours.getCredits());
        } else {
            ps.setNull(6, java.sql.Types.INTEGER);
        }
        ps.setString(7, cours.getLangue());
        ps.setTimestamp(8, timestampOf(cours.getDateCreation()));
        ps.setDate(9, dateOf(cours.getDateDebut()));
        ps.setDate(10, dateOf(cours.getDateFin()));
        ps.setString(11, cours.getStatut());
        ps.setString(12, cours.getImageCoursUrl());
        ps.setString(13, serializeList(cours.getPrerequis()));
    }

    Cours mapCours(ResultSet rs, boolean loadRelations) throws SQLException {
        Cours cours = new Cours();
        ModuleService moduleService = new ModuleService();
        cours.setId(rs.getInt("id"));
        cours.setCodeCours(rs.getString("code_cours"));
        cours.setTitre(rs.getString("titre"));
        cours.setDescription(rs.getString("description"));

        int moduleId = rs.getInt("module_id");
        if (!rs.wasNull()) {
            Module module = moduleService.getShallowById(moduleId);
            cours.setModule(module != null ? module : shallowModule(moduleId));
        }

        cours.setNiveau(rs.getString("niveau"));

        int credits = rs.getInt("credits");
        if (!rs.wasNull()) {
            cours.setCredits(credits);
        }

        cours.setLangue(rs.getString("langue"));

        Timestamp dateCreation = rs.getTimestamp("date_creation");
        cours.setDateCreation(dateCreation != null ? dateCreation.toLocalDateTime() : null);

        Date dateDebut = rs.getDate("date_debut");
        cours.setDateDebut(dateDebut != null ? dateDebut.toLocalDate() : null);

        Date dateFin = rs.getDate("date_fin");
        cours.setDateFin(dateFin != null ? dateFin.toLocalDate() : null);

        cours.setStatut(rs.getString("statut"));
        cours.setImageCoursUrl(rs.getString("image_cours_url"));
        cours.setPrerequis(deserializeList(rs.getString("prerequis")));
        if (loadRelations) {
            ContenuService contenuService = new ContenuService();
            cours.setContenus(contenuService.getByCoursId(cours.getId()));
        }
        return cours;
    }

    Cours getShallowById(int id) throws SQLException {
        String sql = "SELECT * FROM cours WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCours(rs, false);
                }
            }
        }
        return null;
    }

    private Timestamp timestampOf(LocalDateTime value) {
        return value != null ? Timestamp.valueOf(value) : null;
    }

    private Date dateOf(LocalDate value) {
        return value != null ? Date.valueOf(value) : null;
    }

    private String serializeList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.stream()
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.joining(","));
    }

    private List<String> deserializeList(String values) {
        if (values == null || values.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(values.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private Module shallowModule(int id) {
        Module module = new Module();
        module.setId(id);
        return module;
    }

    private boolean tableHasColumns(Connection connection, String tableName, String... columns) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        if (!tableExists(metaData, tableName)) {
            return false;
        }
        for (String column : columns) {
            if (!columnExists(metaData, tableName, column)) {
                return false;
            }
        }
        return true;
    }

    private boolean tableExists(DatabaseMetaData metaData, String tableName) throws SQLException {
        try (ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
            if (rs.next()) {
                return true;
            }
        }
        try (ResultSet rs = metaData.getTables(null, null, tableName.toUpperCase(), new String[]{"TABLE"})) {
            if (rs.next()) {
                return true;
            }
        }
        try (ResultSet rs = metaData.getTables(null, null, tableName.toLowerCase(), new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private boolean columnExists(DatabaseMetaData metaData, String tableName, String columnName) throws SQLException {
        try (ResultSet rs = metaData.getColumns(null, null, tableName, columnName)) {
            if (rs.next()) {
                return true;
            }
        }
        try (ResultSet rs = metaData.getColumns(null, null, tableName.toUpperCase(), columnName.toUpperCase())) {
            if (rs.next()) {
                return true;
            }
        }
        try (ResultSet rs = metaData.getColumns(null, null, tableName.toLowerCase(), columnName.toLowerCase())) {
            return rs.next();
        }
    }

    private List<RelationDefinition> teacherCourseRelations() {
        return List.of(
                new RelationDefinition("cours_enseignant", "cours_id", "enseignant_id"),
                new RelationDefinition("enseignant_cours", "cours_id", "enseignant_id"),
                new RelationDefinition("cours_enseignants", "cours_id", "enseignant_id")
        );
    }

    private List<RelationDefinition> courseStudentRelations() {
        return List.of(
                new RelationDefinition("cours_etudiant", "cours_id", "etudiant_id"),
                new RelationDefinition("etudiant_cours", "cours_id", "etudiant_id"),
                new RelationDefinition("inscription", "cours_id", "etudiant_id"),
                new RelationDefinition("inscriptions", "cours_id", "etudiant_id")
        );
    }

    private void requireId(Integer id, String action) throws SQLException {
        if (id == null) {
            throw new SQLException("Missing cours id for " + action + ".");
        }
    }

    private void ensureTeacherCourseLink(Integer coursId, long teacherId) throws SQLException {
        requireId(coursId, "teacher course link");
        Connection connection = getConnection();
        if (connection == null) {
            throw new SQLException("Database connection is unavailable.");
        }

        RelationDefinition relation = null;
        for (RelationDefinition definition : teacherCourseRelations()) {
            if (tableHasColumns(connection, definition.tableName(), definition.leftColumn(), definition.rightColumn())) {
                relation = definition;
                break;
            }
        }
        if (relation == null) {
            return;
        }

        String checkSql = "SELECT COUNT(*) FROM " + relation.tableName()
                + " WHERE " + relation.leftColumn() + " = ? AND " + relation.rightColumn() + " = ?";
        try (PreparedStatement ps = connection.prepareStatement(checkSql)) {
            ps.setInt(1, coursId);
            ps.setLong(2, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return;
                }
            }
        }

        String insertSql = "INSERT INTO " + relation.tableName()
                + " (" + relation.leftColumn() + ", " + relation.rightColumn() + ") VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
            ps.setInt(1, coursId);
            ps.setLong(2, teacherId);
            ps.executeUpdate();
        }
    }

    private void ensureStudentCourseLink(Integer coursId, long studentId) throws SQLException {
        requireId(coursId, "student course link");
        Connection connection = getConnection();
        if (connection == null) {
            throw new SQLException("Database connection is unavailable.");
        }

        RelationDefinition relation = null;
        for (RelationDefinition definition : courseStudentRelations()) {
            if (tableHasColumns(connection, definition.tableName(), definition.leftColumn(), definition.rightColumn())) {
                relation = definition;
                break;
            }
        }
        if (relation == null) {
            throw new SQLException("No course-student relation table is available.");
        }

        String checkSql = "SELECT COUNT(*) FROM " + relation.tableName()
                + " WHERE " + relation.leftColumn() + " = ? AND " + relation.rightColumn() + " = ?";
        try (PreparedStatement ps = connection.prepareStatement(checkSql)) {
            ps.setInt(1, coursId);
            ps.setLong(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return;
                }
            }
        }

        String insertSql = "INSERT INTO " + relation.tableName()
                + " (" + relation.leftColumn() + ", " + relation.rightColumn() + ") VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
            ps.setInt(1, coursId);
            ps.setLong(2, studentId);
            ps.executeUpdate();
        }
    }

    private record RelationDefinition(String tableName, String leftColumn, String rightColumn) {
    }
}
