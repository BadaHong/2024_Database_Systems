
import java.sql.*;
import java.util.*;

public class Manager {
    private static ResultSet resultSet;
    private static Scanner scan = new Scanner(System.in);
    static Integer currentManagerId;

    public Manager(int managerId) {
        currentManagerId = managerId;
    }

    public void managerMenu() throws SQLException {
        while (true) {
            String input;
            System.out.println("\n\n================================ Menu for Manager ================================");
            System.out.println("1. Manage Users"); // show all user lists, show its own users, edit/delete users
            System.out.println("2. Manage Music"); // show music lists, show music I created, create/edit/delete music
            System.out.println("3. Manage Albums"); // show album lists, create/edit/delete album
            System.out.println("4. My Profile"); // edit username/password
            System.out.println("5. Resign Manager"); // quit the job as manager -> delete from database
            System.out.println("6. Logout"); // exit
            System.out.println("----------------------------------------------------------------------------------");
            System.out.print("Please enter your option: ");
            input = scan.nextLine();

            switch (input) {
                case "1": {
                    manageUsersMenu();
                    break;
                }
                case "2": {
                    manageMusicMenu();
                    break;
                }
                case "3": {
                    manageAlbumMenu();
                    break;
                }
                case "4": {
                    User.myProfile("managers");
                    break;
                }
                case "5": {
                    System.out.println("\n\n==================================== Quit Job ====================================");
                    while (true) {
                        System.out.print("Do you really want to resign and quit the job? (Y/N) ");
                        String quit = scan.nextLine();
                        if (quit.equalsIgnoreCase("N")) {
                            break;
                        } else if (quit.equalsIgnoreCase("Y")) {
                            deleteManager();
                            return;
                        } else {
                            System.out.println("\nWrong Input! Try again...");
                        }
                    }
                    break;
                }
                case "6": {
                    System.out.println("\n\n===================================== Logout =====================================");
                    System.out.print("Do you really want to logout? (Y/N) ");
                    String quit = scan.nextLine();
                    if (quit.equalsIgnoreCase("Y")) {
                        currentManagerId = null;
                        return;
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }

    /* Manage Users */
    private static void manageUsersMenu() throws SQLException {
        while (true) {
            String input;

            System.out.println("\n\n================================== Manage Users ==================================");
            System.out.println("0. Return to previous menu");
            System.out.println("1. Show All Users");
            System.out.println("2. Show Users Managed By Me"); // edit/delete users
            System.out.println("----------------------------------------------------------------------------------");
            System.out.print("Please enter your option: ");
            input = scan.nextLine();

            switch (input) {
                case "0": {
                    return;
                }
                case "1": {
                    showAllUsers();
                    break;
                }
                case "2": {
                    showMyUsers();
                    break;
                }
                default: {
                    System.out.println("\nWrong Input! Try again...");
                    break;
                }
            }
        }
    }

    // Show All Users
    private static void showAllUsers() throws SQLException {
        System.out.println("\n\n================================= Show All Users =================================");
        String sql = "SELECT u.username, m.username AS managername "
                + "FROM users AS u, managers AS m "
                + "WHERE u.managedBy = m.managerId";
        resultSet = Security.sqlResult(sql);
        int count = 0;

        System.out.println("-------------------------------------------------");
        System.out.printf(" %4s  |     %-10s   |  %15s\n", "No.", "USERNAME", "MANAGED BY");
        while (resultSet.next()) {
            count++;
            String username = resultSet.getString("username");
            String managername = resultSet.getString("managername");

            System.out.printf("  %-4s |  %-15s |  %-15s\n", count, username, managername);
        }
        if (count == 0) {
            System.out.println("No users found.");
        }
        System.out.println("-------------------------------------------------");
        System.out.print("Press Enter to go back.");
        scan.nextLine();
    }

    // Show Users Managed By Me
    private static void showMyUsers() throws SQLException {
        while (true) {
            System.out.println("\n\n============================ Show Users Managed By Me ============================");
            String sql = "SELECT u.userId, u.username, m.username AS managername "
                    + "FROM users AS u "
                    + "JOIN managers AS m ON m.managerId = u.managedBy "
                    + "WHERE m.managerId = " + currentManagerId;

            resultSet = Security.sqlResult(sql);
            ArrayList<Integer> userIds = new ArrayList<>();
            int count = 0;

            System.out.println("-------------------------------------------------");
            System.out.printf(" %4s  |     %-10s   \n", "No.", "USERNAME");
            while (resultSet.next()) {
                count++;
                int userId = resultSet.getInt("userId");
                String username = resultSet.getString("username");

                userIds.add(userId);
                System.out.printf("  %-4s |  %-15s \n", count, username);
            }
            if (count == 0) {
                System.out.println("No users found.");
                return;
            }
            System.out.println("-------------------------------------------------");

            while (true) {
                System.out.print("Enter the number of the user to manage (or 0 to return): ");
                int choice = scan.nextInt();
                scan.nextLine();

                if (choice == 0) return;

                if (choice > 0 && choice <= userIds.size()) {
                    int selectedUserId = userIds.get(choice - 1);
                    manageUser(selectedUserId);
                    break;
                } else {
                    System.out.println("Invalid choice! Try again...");
                }
            }
        }
    }

    private static void manageUser(int userId) throws SQLException {
        String sql = "SELECT username FROM users WHERE userId = " + userId;
        resultSet = Security.sqlResult(sql);
        resultSet.next();
        String username = resultSet.getString("username");
        System.out.println("\nUsername: " + username);
        while (true) {
            System.out.println("----------------------------------------------------------------------------------");
            System.out.println("0. Return to previous menu");
            System.out.println("1. Edit Username");
            System.out.println("2. Edit Password");
            System.out.println("3. Delete User");
            System.out.println("4. Change Manager");
            System.out.println("----------------------------------------------------------------------------------");
            System.out.print("Enter your choice: ");
            String choice = scan.nextLine();

            if (choice.equals("0")) return;
            else if (choice.equals("1")) { // Edit Username
                System.out.print("Enter new username: ");
                String newUsername = scan.nextLine();
                String queryCheck = "SELECT COUNT(*) FROM users WHERE username = '" + newUsername + "'";
                resultSet = Security.sqlResult(queryCheck);
                resultSet.next();
                int count = resultSet.getInt("COUNT(*)");

                if (count > 0 && !newUsername.equals(username)) {
                    System.out.println("'" + newUsername + "' is already taken! Please choose a different username.");
                } else {
                    String queryUpdate = "UPDATE users SET username = '" + newUsername + "' WHERE userId = " + userId;
                    Security.sqlUpdate(queryUpdate);
                    System.out.println("Username updated successfully!");
                }
            } else if (choice.equals("2")) { // Edit Password
                System.out.print("Enter new password: ");
                String newPassword = scan.nextLine();
                if (newPassword.isEmpty()) {
                    System.out.println("Password is required. Try again.");
                    continue;
                }
                System.out.print("Enter new password again: ");
                String newPassword_ck = scan.nextLine();
                if (!newPassword.equals(newPassword_ck)) {
                    System.out.println("Passwords do not match! Try again...");
                    continue;
                }
                String hashedPassword = Security.hashPassword(newPassword); // hashing the password for security
                String updateQuery = "UPDATE users SET password = '" + hashedPassword + "' WHERE userId = " + userId;
                Security.sqlUpdate(updateQuery);
                System.out.println("Password reset successfully!");
            } else if (choice.equals("3")) { // Delete User
                System.out.print("Do you really need to delete the user? (Y/N) ");
                String delete = scan.nextLine();
                if (delete.equalsIgnoreCase("Y")) {
                    User.deleteUser(userId);
                    System.out.println("User deleted successfully!");
                    return;
                } else if (!delete.equalsIgnoreCase("N")) {
                    System.out.println("Invalid input! Try again.");
                }
            } else if (choice.equals("4")) { // Change Manager
                if (changeManager(userId, username)) {
                    return;
                }
            } else {
                System.out.println("Invalid choice! Try again...");
            }
        }
    }

    // Change Manager
    private static boolean changeManager(int userId, String userName) throws SQLException {
        String queryManager = "SELECT managerId, username FROM managers";
        resultSet = Security.sqlResult(queryManager);
        ArrayList<Integer> managerIds = new ArrayList<>();
        int count = 0;
        System.out.println("Manager List: ");
        System.out.println("-------------------------------------------------");
        System.out.printf(" %4s  |     %-10s   \n", "No.", "MANAGER NAME");
        while (resultSet.next()) {
            count++;
            int managerId = resultSet.getInt("managerId");
            String managerName = resultSet.getString("username");
            if (managerId == currentManagerId) {
                count--;
                continue;
            }
            managerIds.add(managerId);
            System.out.printf("  %-4s |  %-15s \n", count, managerName);
        }
        if (count == 0) {
            System.out.println("No managers found.");
            return false;
        }
        System.out.println("-------------------------------------------------");

        while (true) {
            System.out.print("Enter the manager number to hand over your authority on the user '" + userName + "' (or 0 to return): ");
            int input = scan.nextInt();
            scan.nextLine();
            if (input == 0) return false;
            if (input > 0 && input <= managerIds.size()) {
                int selectedManagerId = managerIds.get(input - 1);
                String queryUpdate = "UPDATE users SET managedBy = " + selectedManagerId + " WHERE userId = " + userId;
                Security.sqlUpdate(queryUpdate);
                System.out.println("User's manager updated successfully!");
                return true;
            } else {
                System.out.println("Invalid choice! Try again...");
            }
        }
    }

    /* Manage Music */
    private void manageMusicMenu() throws SQLException {
        while (true) {
            String input;
            System.out.println("\n\n================================== Manage Music ==================================");
            System.out.println("0. Return to previous menu");
            System.out.println("1. Show All Music"); // show all the music list & show details (can manage if selected music was created by me)
            System.out.println("2. Show Music I Created"); // show music lists & edit/delete music
            System.out.println("3. Search For Music"); // search music & show details for selected music
            System.out.println("4. Create Music"); // create new music
            System.out.println("----------------------------------------------------------------------------------");
            System.out.print("Please enter your option: ");
            input = scan.nextLine();

            switch (input) {
                case "0": {
                    return;
                }
                case "1": {
                    showAllMusic();
                    break;
                }
                case "2": {
                    showMyMusic();
                    break;
                }
                case "3":
                    User.searchForMusic();
                    break;
                case "4":
                    createMusic();
                    break;
                default: {
                    System.out.println("\nWrong Input! Try again...");
                    break;
                }
            }
        }
    }

    // Show All Music
    private static void showAllMusic() throws SQLException {
        while (true) {
            System.out.println("\n\n================================= Show All Music =================================");
            String sql = "SELECT m.musicId, m.title, m.releaseDate, GROUP_CONCAT(a.artistName SEPARATOR ', ') AS artists "
                    + "FROM music m "
                    + "LEFT JOIN artists a ON m.musicID = a.musicId "
                    + "GROUP BY m.musicId";
            resultSet = Security.sqlResult(sql);

            ArrayList<Integer> musicIds = new ArrayList<>();
            int count = 0;

            System.out.println("\nList of All Music:");
            System.out.println("----------------------------------------------------------------------------------");
            System.out.printf(" %4s  |  %-25s |  %-25s |  %-25s\n", "No.", "TITLE", "ARTISTS", "RELEASE DATE");
            while (resultSet.next()) {
                count++;
                int musicId = resultSet.getInt("musicId");
                String title = resultSet.getString("title");
                String artists = resultSet.getString("artists");
                String releaseDate = resultSet.getString("releaseDate");

                musicIds.add(musicId);

                System.out.printf("  %-4s |  %-25s |  %-25s |  %-25s\n", count, title, artists, releaseDate);
            }
            if (count == 0) {
                System.out.println("No music found.");
                return;
            }

            while (true) { // for detailed music info
                System.out.println("----------------------------------------------------------------------------------");
                System.out.print("Enter the number of the music to see its details (or 0 to return): ");
                int choice = scan.nextInt();
                scan.nextLine();

                if (choice == 0) return;

                if (choice > 0 && choice <= musicIds.size()) {
                    int selectedMusicId = musicIds.get(choice - 1);
                    showMusicDetails(selectedMusicId);
                    String queryManaged = "SELECT COUNT(*) FROM music WHERE musicId = " + selectedMusicId + " AND managedBy = " + currentManagerId;
                    resultSet = Security.sqlResult(queryManaged);
                    if (resultSet.next()) {
                        if (resultSet.getInt(1) == 1) { // if its manager is me
                            while (true) {
                                System.out.print("Do you want to delete or edit the music? (D/E or 0 to return) ");
                                String input = scan.nextLine();
                                if (input.equalsIgnoreCase("0")) break;
                                if (input.equalsIgnoreCase("D")) {
                                    deleteMusic(selectedMusicId);
                                    System.out.println("Music deleted successfully.");
                                    break;
                                } else if (input.equalsIgnoreCase("E")) {
                                    editMusic(selectedMusicId);
                                    break;
                                } else {
                                    System.out.println("Invalid input. Try again.");
                                }
                            }
                        } else { // else just go back
                            System.out.print("Press Enter to go back.");
                            scan.nextLine();
                        }
                    }
                    break;
                } else {
                    System.out.println("Invalid choice! Try again...");
                }
            }
        }
    }

    // show the details of music
    static void showMusicDetails(int musicId) throws SQLException {
        String queryMusicDetails = "SELECT * FROM music WHERE musicId = " + musicId;
        String queryArtists = "SELECT artistName FROM artists WHERE musicId = " + musicId;
        ResultSet musicResult = Security.sqlResult(queryMusicDetails);
        ResultSet artistResult = Security.sqlResult(queryArtists);

        if (musicResult.next()) {
            System.out.println("\nMusic Details:");
            System.out.println("----------------------------------------------------");
            String title = musicResult.getString("title");
            StringBuilder artists = new StringBuilder();
            boolean first = true;
            while (artistResult.next()) {
                if (!first) {
                    artists.append(", ");
                }
                artists.append(artistResult.getString("artistName"));
                first = false;
            }
            String lyricist = musicResult.getString("lyricist");
            String composer = musicResult.getString("composer");
            String genre = musicResult.getString("genre");
            String releaseDate = musicResult.getString("releaseDate");
            String album = " -- ";
            if (musicResult.getInt("albumId") > 0) {
                String queryAlbum = "SELECT title FROM albums WHERE albumId = " + musicResult.getInt("albumId");
                ResultSet albumResult = Security.sqlResult(queryAlbum);
                if (albumResult.next()) {
                    album = albumResult.getString("title");
                } else {
                    album = " -- ";
                }
            }
            System.out.printf("|  %-12s  |  %-30s |\n", "TITLE", title);
            System.out.printf("|  %-12s  |  %-30s |\n", "ARTISTS", artists);
            System.out.printf("|  %-12s  |  %-30s |\n", "LYRICIST", lyricist);
            System.out.printf("|  %-12s  |  %-30s |\n", "COMPOSER", composer);
            System.out.printf("|  %-12s  |  %-30s |\n", "GENRE", genre);
            System.out.printf("|  %-12s  |  %-30s |\n", "RELEASE DATE", releaseDate);
            System.out.printf("|  %-12s  |  %-30s |\n", "IN ALBUM", album);
            System.out.println("----------------------------------------------------");

            System.out.println("Lyrics:");
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            System.out.println(musicResult.getString("lyrics"));
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        }
        System.out.println();
    }

    // delete music managed by me
    private static void deleteMusic(int musicId) throws SQLException {
        String deleteQuery = "DELETE FROM addMusic WHERE musicId = " + musicId;
        Security.sqlUpdate(deleteQuery);
        deleteQuery = "DELETE FROM artists WHERE musicId = " + musicId;
        Security.sqlUpdate(deleteQuery);
        deleteQuery = "DELETE FROM likes WHERE musicId = " + musicId;
        Security.sqlUpdate(deleteQuery);
        deleteQuery = "DELETE FROM plays WHERE musicId = " + musicId;
        Security.sqlUpdate(deleteQuery);
        deleteQuery = "DELETE FROM music WHERE musicId = " + musicId;
        Security.sqlUpdate(deleteQuery);
    }

    // edit music managed by me
    static void editMusic(int musicId) throws SQLException {
        String getMusicQuery = "SELECT * FROM music m " +
                "LEFT JOIN artists a ON m.musicId = a.musicId " +
                "WHERE m.musicId = " + musicId;
        resultSet = Security.sqlResult(getMusicQuery);

        StringBuilder artists = new StringBuilder();

        String title = null, lyricist = null, composer = null, genre = null, lyrics = null, releaseDate = null;
        while (resultSet.next()) {
            title = resultSet.getString("title");
            lyricist = resultSet.getString("lyricist");
            composer = resultSet.getString("composer");
            genre = resultSet.getString("genre");
            lyrics = resultSet.getString("lyrics");
            releaseDate = resultSet.getString("releaseDate");
            if (resultSet.getString("artistName") != null) {
                if (artists.length() > 0) artists.append(", ");
                artists.append(resultSet.getString("artistName"));
            }
        }

        if (title == null) {
            System.out.println("Music not found.");
            return;
        }

        // show current details
        System.out.println("\n\nCurrent Music Details:");
        System.out.println("----------------------------------------------------");
        System.out.printf("|  %-12s  |  %-30s |\n", "TITLE", title);
        System.out.printf("|  %-12s  |  %-30s |\n", "ARTISTS", artists);
        System.out.printf("|  %-12s  |  %-30s |\n", "LYRICIST", lyricist);
        System.out.printf("|  %-12s  |  %-30s |\n", "COMPOSER", composer);
        System.out.printf("|  %-12s  |  %-30s |\n", "GENRE", genre);
        System.out.printf("|  %-12s  |  %-30s |\n", "RELEASE DATE", releaseDate);
        System.out.println("----------------------------------------------------");
        System.out.println("Lyrics:");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println(lyrics);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("----------------------------------------------------------------------------------");

        System.out.println("\nPress Enter to keep current value.");
        System.out.print("Enter new title: ");
        String newTitle = scan.nextLine();
        System.out.print("Enter new artists (comma-separated) - Ex) \"Bada, BTS, TWICE\" : ");
        String newArtists = scan.nextLine();
        System.out.print("Enter new lyricist: ");
        String newLyricist = scan.nextLine();
        System.out.print("Enter new composer: ");
        String newComposer = scan.nextLine();
        System.out.print("Enter new genre: ");
        String newGenre = scan.nextLine();
        System.out.print("Enter new lyrics: ");
        String newLyrics = scan.nextLine();
        System.out.print("Enter new release date (YYYY-MM-DD): ");
        String newReleaseDate = scan.nextLine();

        String updateMusicQuery = "UPDATE music SET title = ?, lyricist = ?, composer = ?, genre = ?, lyrics = ?, releaseDate = ? WHERE musicId = ?";
        try (PreparedStatement psUpdateMusic = Security.connection.prepareStatement(updateMusicQuery)) {
            psUpdateMusic.setString(1, newTitle.isEmpty() ? title : newTitle);
            psUpdateMusic.setString(2, newLyricist.isEmpty() ? lyricist : newLyricist);
            psUpdateMusic.setString(3, newComposer.isEmpty() ? composer : newComposer);
            psUpdateMusic.setString(4, newGenre.isEmpty() ? genre : newGenre);
            psUpdateMusic.setString(5, newLyrics.isEmpty() ? lyrics : newLyrics);
            psUpdateMusic.setString(6, newReleaseDate.isEmpty() ? releaseDate : newReleaseDate);
            psUpdateMusic.setInt(7, musicId);
            psUpdateMusic.executeUpdate();
        }

        if (!newArtists.isEmpty()) {
            String deleteArtistsQuery = "DELETE FROM artists WHERE musicId = " + musicId;
            Security.sqlUpdate(deleteArtistsQuery);

            String[] artistNames = newArtists.split(",");
            String insertArtistQuery = "INSERT INTO artists (musicId, artistName) VALUES (?, ?)";
            try (PreparedStatement psInsertArtist = Security.connection.prepareStatement(insertArtistQuery)) {
                for (String artistName : artistNames) {
                    psInsertArtist.setInt(1, musicId);
                    psInsertArtist.setString(2, artistName.trim());
                    psInsertArtist.executeUpdate();
                }
            }
        }
        System.out.println("Music information updated successfully!");
    }

    // Show Music I Created
    private static void showMyMusic() throws SQLException {
        while (true) {
            System.out.println("\n\n============================== Show Music I Created ==============================");
            String queryMyMusic = "SELECT m.musicId, m.title, m.releaseDate, GROUP_CONCAT(a.artistName SEPARATOR ', ') AS artists "
                    + "FROM music AS m "
                    + "LEFT JOIN artists AS a ON m.musicID = a.musicId "
                    + "WHERE m.managedBy = " + currentManagerId
                    + " GROUP BY m.musicId "
                    + "ORDER BY m.title";

            resultSet = Security.sqlResult(queryMyMusic);

            ArrayList<Integer> musicIds = new ArrayList<>();
            int count = 0;

            System.out.println("\nList of Music I Created:");
            System.out.println("----------------------------------------------------------------------------------");
            System.out.printf(" %4s  |  %-25s |  %-25s |  %-25s\n", "No.", "TITLE", "ARTISTS", "RELEASE DATE");
            while (resultSet.next()) {
                count++;
                int musicId = resultSet.getInt("musicId");
                String title = resultSet.getString("title");
                String artists = resultSet.getString("artists");
                String releaseDate = resultSet.getString("releaseDate");

                musicIds.add(musicId);
                System.out.printf("  %-4s |  %-25s |  %-25s |  %-25s\n", count, title, artists, releaseDate);
            }
            if (count == 0) {
                System.out.println("No music found.");
                return;
            }
            while (true) {
                System.out.println("----------------------------------------------------------------------------------");
                System.out.print("Enter the number of the music you want to manage (or 0 to return): ");
                int choice = scan.nextInt();
                scan.nextLine();

                if (choice == 0) return;

                if (choice > 0 && choice <= musicIds.size()) {
                    int selectedMusicId = musicIds.get(choice - 1);
                    while (true) {
                        System.out.print("Do you want to delete or edit the music? (D/E or 0 to return) ");
                        String input = scan.nextLine();
                        if (input.equalsIgnoreCase("0")) break;
                        if (input.equalsIgnoreCase("D")) {
                            deleteMusic(selectedMusicId);
                            System.out.println("Music deleted successfully.");
                            break;
                        } else if (input.equalsIgnoreCase("E")) {
                            editMusic(selectedMusicId);
                            break;
                        } else {
                            System.out.println("Invalid input. Try again.");
                        }
                    }
                    break;
                } else {
                    System.out.println("Invalid choice! Try again...");
                }
            }
        }
    }

    // Create Music
    private static void createMusic() throws SQLException {
        System.out.println("\n\n================================== Create Music ==================================");
        System.out.print("Is this music part of an album? (Y/N): ");
        String isPartOfAlbum = scan.nextLine().trim().toLowerCase();

        Integer albumId = null;
        if (isPartOfAlbum.equals("y")) {
            String albumTitle;
            while (true) {
                System.out.print("Enter album title: ");
                albumTitle = scan.nextLine();
                if (albumTitle.isEmpty()) {
                    System.out.println("Title is required. Try again.");
                } else {
                    break;
                }
            }

            System.out.print("Enter album release date (YYYY-MM-DD): ");
            String albumReleaseDate = scan.nextLine();

            // find albumId
            String findAlbumQuery = "SELECT albumId FROM albums WHERE title = '" + albumTitle + "' AND releaseDate = '" + albumReleaseDate + "'";
            resultSet = Security.connection.createStatement().executeQuery(findAlbumQuery);

            if (resultSet.next()) {
                albumId = resultSet.getInt("albumId");
            } else {
                System.out.println("----------------------------------------------------------------------------------");
                System.out.println("No such an album found. Please create the album first.");
                return; // exit if the album doesn't exist
            }
        } else if (!isPartOfAlbum.equals("n")) {
            System.out.println("Invalid input. Try again.");
            return;
        }

        String title;
        while (true) { // title attribute is NOT NULL in music table
            System.out.print("Enter music title: ");
            title = scan.nextLine();
            if (title.isEmpty()) {
                System.out.println("Title is required. Try again.");
            } else {
                break;
            }
        }

        System.out.print("Enter music genre: ");
        String genre = scan.nextLine();

        String releaseDate;
        while (true) {
            System.out.print("Enter music release date (YYYY-MM-DD): ");
            releaseDate = scan.nextLine();
            if (releaseDate.isEmpty()) {
                System.out.println("Release date is required. Try again.");
            } else {
                break;
            }
        }

        System.out.print("Enter lyricist: ");
        String lyricist = scan.nextLine();

        System.out.print("Enter composer: ");
        String composer = scan.nextLine();

        System.out.print("Enter lyrics (type 'END' on a new line to finish): ");
        StringBuilder lyricsBuilder = new StringBuilder();
        while (true) {
            String line = scan.nextLine();
            if (line.equalsIgnoreCase("END")) break;
            lyricsBuilder.append(line).append("\n");
        }
        String lyrics = lyricsBuilder.toString().trim();

        System.out.print("Enter artist names (comma-separated): ");
        String artistNames = scan.nextLine();

        String insertMusicQuery = "INSERT INTO music (title, genre, releaseDate, lyricist, composer, lyrics, albumId, managedBy) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement psInsertMusic = Security.connection.prepareStatement(insertMusicQuery, Statement.RETURN_GENERATED_KEYS)) {
            psInsertMusic.setString(1, title);
            psInsertMusic.setString(2, genre);
            psInsertMusic.setString(3, releaseDate);
            psInsertMusic.setString(4, lyricist);
            psInsertMusic.setString(5, composer);
            psInsertMusic.setString(6, lyrics);
            if (albumId != null) {
                psInsertMusic.setInt(7, albumId);
            } else {
                psInsertMusic.setNull(7, Types.INTEGER);
            }
            psInsertMusic.setInt(8, currentManagerId);

            psInsertMusic.executeUpdate();

            ResultSet rs = psInsertMusic.getGeneratedKeys();
            if (rs.next()) {
                int musicId = rs.getInt(1);

                String[] artists = artistNames.split(",");
                String insertArtistQuery = "INSERT INTO artists (musicId, artistName) VALUES (?, ?)";
                try (PreparedStatement psInsertArtist = Security.connection.prepareStatement(insertArtistQuery)) {
                    for (String artist : artists) {
                        psInsertArtist.setInt(1, musicId);
                        psInsertArtist.setString(2, artist.trim());
                        psInsertArtist.executeUpdate();
                    }
                }
                System.out.println("Music created successfully!");
            } else {
                System.out.println("Failed to retrieve the music ID.");
            }
        }
    }

    /* Manage Albums */
    private void manageAlbumMenu() throws SQLException {
        while (true) {
            String input;

            System.out.println("\n\n================================== Manage Albums =================================");
            System.out.println("0. Return to previous menu");
            System.out.println("1. Show All Album"); // show all the album list & can edit
            System.out.println("2. Create Album"); // create new album
            System.out.println("----------------------------------------------------------------------------------");
            System.out.print("Please enter your option: ");
            input = scan.nextLine();

            switch (input) {
                case "0": {
                    return;
                }
                case "1": {
                    showAllAlbum();
                    break;
                }
                case "2": {
                    createAlbum();
                    break;
                }
                default: {
                    System.out.println("\nWrong Input! Try again...");
                    break;
                }
            }
        }
    }

    // Show All Album
    static void showAllAlbum() throws SQLException {
        while (true) {
            System.out.println("\n\n================================ Show All Albums =================================\n");
            String queryAlbum = "SELECT * FROM albums";
            resultSet = Security.sqlResult(queryAlbum);

            String query = """
                        SELECT 
                            al.albumId, 
                            al.title AS albumTitle, 
                            al.releaseDate, 
                            m.title AS musicTitle, 
                            GROUP_CONCAT(DISTINCT a.artistName SEPARATOR ', ') AS artists 
                        FROM albums AS al 
                        LEFT JOIN music AS m ON al.albumId = m.albumId 
                        LEFT JOIN artists AS a ON m.musicId = a.musicId 
                        GROUP BY al.albumId, m.musicId 
                        ORDER BY al.title;
                    """; // GROUP_CONCAT() for merging artists with ", " for the same musicId
            resultSet = Security.sqlResult(query);

            Map<Integer, String> albums = new LinkedHashMap<>();
            Map<Integer, List<String>> albumMusicTitles = new LinkedHashMap<>();
            Map<Integer, List<String>> albumArtists = new LinkedHashMap<>();
            int albumExists = 0;

            while (resultSet.next()) {
                albumExists = 1;
                int albumId = resultSet.getInt("albumId");
                String albumTitle = resultSet.getString("albumTitle");
                String releaseDate = resultSet.getString("releaseDate");
                String musicTitle = resultSet.getString("musicTitle");
                String artists = resultSet.getString("artists");

                albums.putIfAbsent(albumId, albumTitle + " (Released on " + releaseDate + ")");
                albumMusicTitles.computeIfAbsent(albumId, k -> new ArrayList<>()).add(musicTitle != null ? musicTitle : "No Music Exists");
                albumArtists.computeIfAbsent(albumId, k -> new ArrayList<>()).add(artists != null ? artists : "Unknown Artist");
            }

            if (albumExists == 0) {
                System.out.println("No albums found.");
                return;
            }
            ArrayList<Integer> albumIds = new ArrayList<>(albums.keySet());
            int count = 0;
            for (int albumId : albumIds) {
                count++;
                System.out.println(count + ". Title: " + albums.get(albumId));
                System.out.println("---------------------------------------------------------------");
                System.out.printf(" %-25s  |  %-25s \n", "MUSIC TITLE", "ARTISTS");

                List<String> musicTitles = albumMusicTitles.get(albumId);
                List<String> artists = albumArtists.get(albumId);

                for (int i = 0; i < musicTitles.size(); i++) {
                    if (i == 0 && musicTitles.get(i).equals("No Music Exists")){
                        System.out.println("No Music Exists");
                        continue;
                    }
                    System.out.printf(" %-25s  |  %-25s \n", musicTitles.get(i), artists.get(i));
                }
                System.out.println("---------------------------------------------------------------");
            }

            if (currentManagerId != null && User.currentUserId == null) { // when it's Manager
                while (true) { // manage album
                    System.out.println("----------------------------------------------------------------------------------");
                    System.out.print("Enter the number of the album to manage (or 0 to return): ");
                    int choice = scan.nextInt();
                    scan.nextLine();

                    if (choice == 0) return;

                    if (choice > 0 && choice <= albumIds.size()) {
                        int selectedAlbumId = albumIds.get(choice - 1);
                        while (true) {
                            System.out.println("0. Return to previous menu");
                            System.out.println("1. Edit Album");
                            System.out.println("2. Delete Album");
                            System.out.println("3. Add Music To Album");
                            System.out.println("4. Remove Music From Album");
                            System.out.println("----------------------------------------------------------------------------------");
                            System.out.print("Enter your option: ");
                            String input = scan.nextLine();

                            if (input.equals("0")) return;
                            if (input.equals("1")) { // Edit Album
                                editAlbum(selectedAlbumId);
                                break;
                            } else if (input.equals("2")) { // Delete Album
                                deleteAlbum(selectedAlbumId);
                                break;
                            } else if (input.equals("3")) { // Add Music To Album
                                addMusicToAlbum(selectedAlbumId);
                                break;
                            } else if (input.equals("4")) { // Remove Music From Album
                                List<String> musicList = albumMusicTitles.get(selectedAlbumId);
                                List<String> artistList = albumArtists.get(selectedAlbumId);
                                deleteMusicFromAlbum(selectedAlbumId, musicList, artistList);
                                break;
                            } else {
                                System.out.println("Invalid input! Try again...");
                            }
                        }
                        break;
                    } else {
                        System.out.println("Invalid choice! Try again...");
                    }
                }
            } else if (currentManagerId == null && User.currentUserId != null) { // when it's User
                while (true) {
                    System.out.println("----------------------------------------------------------------------------------");
                    System.out.print("Enter the number of the album to see its details (or 0 to return): ");
                    int choice = scan.nextInt();
                    scan.nextLine();

                    if (choice == 0) return;

                    if (choice > 0 && choice <= albumIds.size()) {
                        int selectedAlbumId = albumIds.get(choice - 1);
                        User.showAlbumDetails(selectedAlbumId);
                        break;
                    } else {
                        System.out.println("Invalid choice! Try again...");
                    }
                }
            } else {
                System.out.println("Invalid choice! Try again...");
            }
        }
    }

    private static void editAlbum(int albumId) throws SQLException {
        String sql = "SELECT * FROM albums WHERE albumId = " + albumId;
        resultSet = Security.sqlResult(sql);

        String title = null, releaseDate = null;
        while (resultSet.next()) {
            title = resultSet.getString("title");
            releaseDate = resultSet.getString("releaseDate");
        }

        if (title == null) {
            System.out.println("Album not found.");
            return;
        }

        // Display current details
        System.out.println("----------------------------------------------------------------------------------");
        System.out.println("\nCurrent Album Details:");
        System.out.println("---------------------------------------------------------------");
        System.out.printf(" %-25s  |  %-25s \n", "TITLE", "RELEASE DATE");
        System.out.printf(" %-25s  |  %-25s \n", title, releaseDate);
        System.out.println("---------------------------------------------------------------");
        System.out.println();

        System.out.println("Press Enter to keep current value.");
        System.out.print("Enter new title: ");
        String newTitle = scan.nextLine();
        System.out.print("Enter new release date (YYYY-MM-DD): ");
        String newReleaseDate = scan.nextLine();

        String finalTitle = newTitle.isEmpty() ? title : newTitle;
        String finalReleaseDate = newReleaseDate.isEmpty() ? releaseDate : newReleaseDate;
        String updateAlbumQuery = "UPDATE albums SET title = '" + finalTitle + "', releaseDate = '" + finalReleaseDate + "' WHERE albumId = " + albumId;
        Security.sqlUpdate(updateAlbumQuery);

        System.out.println("Album information updated successfully!");
    }

    private static void deleteAlbum(int albumId) throws SQLException {
        System.out.print("Do you really want to delete this album? (Y/N) ");
        String delete = scan.nextLine();
        if (delete.equalsIgnoreCase("N")) return;
        else if (delete.equalsIgnoreCase("Y")) {
            String sql = "UPDATE music SET albumId = null WHERE albumId = " + albumId;
            Security.sqlUpdate(sql);

            sql = "DELETE FROM albums where albumId = " + albumId;
            Security.sqlUpdate(sql);

            System.out.println("Album deleted successfully!");
        } else {
            System.out.println("Invalid input! Try again.");
        }
    }

    private static void addMusicToAlbum(int albumId) throws SQLException {
        while (true) {
            System.out.println("Enter the music details to add (or 0 to return)");

            System.out.print("Music Title: ");
            String musicTitle = scan.nextLine();
            if (musicTitle.equals("0")) return;

            System.out.print("Release Date (YYYY-MM-DD): ");
            String releaseDate = scan.nextLine();
            if (releaseDate.equals("0")) return;

            System.out.print("Artists (comma-separated): ");
            String artists = scan.nextLine();
            if (artists.equals("0")) return;

            String queryCheck = "SELECT m.musicId, m.albumId, GROUP_CONCAT(DISTINCT a.artistName SEPARATOR ', ') AS artists "
                    + "FROM music m "
                    + "LEFT JOIN artists a ON m.musicId = a.musicId "
                    + "WHERE m.title = '" + musicTitle + "' AND m.releaseDate = '" + releaseDate + "' "
                    + "GROUP BY m.musicId";
            resultSet = Security.sqlResult(queryCheck);

            if (resultSet.next()) {
                int musicId = resultSet.getInt("musicId");
                int albumIdInDB = resultSet.getInt("albumId");
                String artistInDB = resultSet.getString("artists");
                if (artistInDB == null || !artistInDB.equalsIgnoreCase(artists.trim())) {
                    System.out.println("No such music found. Try again.");
                } else {
                    if (albumIdInDB == albumId) {
                        System.out.println("Music already exists in this album.");
                        return;
                    }
                    String queryUpdate = "UPDATE music SET albumId = " + albumId + " WHERE musicId = " + musicId;
                    Security.sqlUpdate(queryUpdate);
                    System.out.println("Music added to album successfully!");
                    return;
                }
            } else {
                System.out.println("No such music found. Try again.");
            }
        }
    }

    private static void deleteMusicFromAlbum(int albumId, List<String> musicList, List<String> artistList) throws SQLException {
        System.out.println("\nMusic List in Album: ");
        System.out.println("----------------------------------------------------------------------------------");
        System.out.printf(" %4s  |  %-25s |  %-25s \n", "No.", "TITLE", "ARTISTS");
        int count2 = 0;
        for (int i = 0; i < musicList.size(); i++) {
            count2++;
            if (count2 == 1 && musicList.get(i).equals("No Music Exists")){
                System.out.println("No music to delete.");
                System.out.println("----------------------------------------------------------------------------------");
                return;
            }
            System.out.printf("  %-4s |  %-25s |  %-25s \n", count2, musicList.get(i), artistList.get(i));
        }

        if (musicList.isEmpty()) {
            System.out.println("No music to delete.");
            System.out.println("----------------------------------------------------------------------------------");
            return;
        }
        if (count2 == 0) {
            System.out.println("No music to delete.");
            System.out.println("----------------------------------------------------------------------------------");
            return;
        } else {
            System.out.println("----------------------------------------------------------------------------------");
            System.out.print("\nEnter the number of the music to delete (or 0 to return): ");
            int input2 = scan.nextInt();
            scan.nextLine();
            if (input2 == 0) return;

            if (input2 > 0 && input2 <= musicList.size()) {
                String selectedMusic = musicList.get(input2 - 1);
                String selectedArtist = artistList.get(input2 - 1);
                String queryMusic = "SELECT m.musicId, GROUP_CONCAT(DISTINCT a.artistName SEPARATOR ', ') AS artists "
                        + "FROM music m "
                        + "LEFT JOIN artists a ON m.musicId = a.musicId "
                        + "WHERE m.title = '" + selectedMusic + "' "
                        + "GROUP BY m.musicId";
                resultSet = Security.sqlResult(queryMusic);
                boolean found = false;
                while (resultSet.next()) {
                    int musicId = resultSet.getInt("musicId");
                    String artistsInDB = resultSet.getString("artists");
                    if (artistsInDB != null && artistsInDB.equalsIgnoreCase(selectedArtist)) {
                        String queryDelete = "UPDATE music SET albumId = NULL WHERE musicId = " + musicId;
                        Security.sqlUpdate(queryDelete);
                        System.out.println("Music deleted from album successfully!");
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    System.out.println("No such music found. Try again.");
                }
            } else {
                System.out.println("Invalid choice! Try again...");
            }
        }
    }

    // Create Album
    private void createAlbum() throws SQLException {
        System.out.println("\n\n================================== Create Album ==================================");
        System.out.println("Enter 0 if you want to go back.");
        String albumTitle;
        while (true) {
            System.out.print("Enter new album's title: ");
            albumTitle = scan.nextLine();
            if (albumTitle.isEmpty()) {
                System.out.println("Title is required. Try again.");
            } else if (albumTitle.equals("0")) {
                return;
            } else {
                break;
            }
        }

        System.out.print("Enter new album's release date (YYYY-MM-DD): ");
        String albumReleaseDate = scan.nextLine();
        if (albumReleaseDate.equals("0")) {
            return;
        }

        String insertMusicQuery = "INSERT INTO albums (title, releaseDate) VALUES ('" + albumTitle + "', '" + albumReleaseDate + "')";
        Security.sqlUpdate(insertMusicQuery);
        System.out.println("Album created successfully!");
    }

    /* Resign */
    private void deleteManager() throws SQLException {
        String sql = "SELECT managerId FROM managers";
        resultSet = Security.sqlResult(sql);

        ArrayList<Integer> managerIds = new ArrayList<>();
        int id;
        while (resultSet.next()) {
            id = resultSet.getInt("managerId");
            if (id != currentManagerId) {
                managerIds.add(id);
            }
        }
        if (!managerIds.isEmpty()) {
            String queryUpdateUser = "UPDATE users SET managedBy = " + managerIds.get((int) (Math.random() * managerIds.size())) + " WHERE managedBy = " + currentManagerId;
            String queryUpdateMusic = "UPDATE music SET managedBy = " + managerIds.get((int) (Math.random() * managerIds.size())) + " WHERE managedBy = " + currentManagerId;
            String queryDelete = "DELETE FROM managers WHERE managerId = " + currentManagerId;

            Security.sqlUpdate(queryUpdateUser);
            Security.sqlUpdate(queryUpdateMusic);
            Security.sqlUpdate(queryDelete);

            System.out.println("Okay... Respect your decision. It was an honor to work with you. Goodbye...");
        } else {
            System.out.println("No other managers found. Resignation aborted.");
        }

    }
}
