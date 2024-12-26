import java.sql.*;
import java.util.*;

public class User {
    private static ResultSet resultSet;
    private static Scanner scan = new Scanner(System.in);
    static Integer currentUserId;

    public User(int userId) {
        currentUserId = userId;
    }

    public void userMenu() throws SQLException {
        while (true) {
            String input;

            System.out.println("\n\n================================== Menu for User =================================");
            System.out.println("1. Music Charts"); // recent music, most listened/liked music
            System.out.println("2. Search for Music"); // each music has detail info page & can like each music
            System.out.println("3. Albums"); // show all albums with music info
            System.out.println("4. Playlists"); // Create New Playlist, My Playlists, Edit Playlist, See Others' Playlists
            System.out.println("5. Liked Music"); // show liked music list
            System.out.println("6. My Profile"); // edit username/password
            System.out.println("7. Logout"); // exit
            System.out.println("8. Sign out"); // delete user
            System.out.println("----------------------------------------------------------------------------------");
            System.out.print("Please enter your option: ");
            input = scan.nextLine();

            switch (input) {
                case "1": {
                    musicChartsMenu();
                    break;
                }
                case "2": {
                    searchForMusic();
                    break;
                }
                case "3": {
                    Manager.showAllAlbum();
                    break;
                }
                case "4": {
                    playlistMenu();
                    break;
                }
                case "5": {
                    likedMusic();
                    break;
                }
                case "6": {
                    myProfile("users");
                    break;
                }
                case "7": {
                    System.out.println("\n\n===================================== Logout =====================================");
                    System.out.print("Do you really want to logout? (Y/N) ");
                    String quit = scan.nextLine();
                    if (quit.equalsIgnoreCase("Y")) {
                        currentUserId = null;
                        return;
                    }
                    break;
                }
                case "8": {
                    System.out.println("\n\n==================================== Sign Out ====================================");
                    while (true) {
                        System.out.print("Do you really want to sign out? (Y/N) ");
                        String quit = scan.nextLine();
                        if (quit.equalsIgnoreCase("Y")) {
                            deleteUser(currentUserId);
                            System.out.println("Hope to see you again! Good Bye...");
                            return;
                        } else if (quit.equalsIgnoreCase("N")) {
                            break;
                        } else {
                            System.out.println("Invalid input. Try again.");
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }

    /* Music Charts */
    private static void musicChartsMenu() throws SQLException {
        while (true) {
            String input;

            System.out.println("\n\n================================== Music Charts ==================================");
            System.out.println("0. Return to previous menu");
            System.out.println("1. Most Recent Music");
            System.out.println("2. Most Listened Music");
            System.out.println("3. Most Liked Music");
            System.out.println("----------------------------------------------------------------------------------");
            System.out.print("Please enter your option: ");
            input = scan.nextLine();

            switch (input) {
                case "0": {
                    return;
                }
                case "1": {
                    mostRecentMusic();
                    break;
                }
                case "2": {
                    mostListenedMusic();
                    break;
                }
                case "3": {
                    mostLikedMusic();
                    break;
                }
                default: {
                    System.out.println("\nWrong Input! Try again...");
                    break;
                }
            }
        }
    }

    // Most Recent Music
    private static void mostRecentMusic() throws SQLException {
        while (true) {
            String sql = "SELECT m.musicId, m.title, m.releaseDate, GROUP_CONCAT(DISTINCT a.artistName SEPARATOR ', ') AS artists "
                    + "FROM music m "
                    + "LEFT JOIN artists a ON m.musicID = a.musicId "
                    + "GROUP BY m.musicId "
                    + "ORDER BY m.releaseDate DESC"; // order by releaseDate in descending order
            resultSet = Security.sqlResult(sql);

            ArrayList<Integer> musicIds = new ArrayList<>();
            int count = 1;

            System.out.println("\n============================== Most Recent Music Chart =============================");
            System.out.println("----------------------------------------------------------------------------------");
            System.out.printf(" %4s  |  %-25s |  %-25s |  %-25s\n", "No.", "TITLE", "ARTISTS", "RELEASE DATE");
            while (resultSet.next()) {
                int musicId = resultSet.getInt("musicId");
                String title = resultSet.getString("title");
                String artists = resultSet.getString("artists");
                String releaseDate = resultSet.getString("releaseDate");

                musicIds.add(musicId);
                System.out.printf("  %-4s |  %-25s |  %-25s |  %-25s\n", count, title, artists, releaseDate);
                count++;
            }
            System.out.println("----------------------------------------------------------------------------------\n");

            while (true) {
                System.out.print("Enter the number of the music to see its details (or 0 to return): ");
                int choice = scan.nextInt();
                scan.nextLine();

                if (choice == 0) return;

                if (choice > 0 && choice <= musicIds.size()) {
                    int selectedMusicId = musicIds.get(choice - 1);
                    userMusicFeatures(selectedMusicId);
                    break;
                } else {
                    System.out.println("Invalid choice! Try again...");
                }
            }
        }
    }

    // Most Listened Music
    private static void mostListenedMusic() throws SQLException {
        while (true) {
            String sql = "SELECT m.musicId, m.title, m.releaseDate, GROUP_CONCAT(DISTINCT a.artistName SEPARATOR ', ') AS artists, "
                    + "IFNULL(tp.totalPlays, 0) AS totalPlays "
                    + "FROM music m "
                    + "LEFT JOIN ("
                    + "SELECT musicId, SUM(numOfPlays) AS totalPlays "
                    + "FROM plays GROUP BY musicId"
                    + ") AS tp ON tp.musicId = m.musicId "
                    + "LEFT JOIN artists a ON m.musicID = a.musicId "
                    + "GROUP BY m.musicId "
                    + "ORDER BY totalPlays DESC";
            resultSet = Security.sqlResult(sql);

            ArrayList<Integer> musicIds = new ArrayList<>();
            int count = 0;

            System.out.println("\n============================= Most Listened Music Chart ============================");
            System.out.println("----------------------------------------------------------------------------------");
            System.out.printf(" %4s  |  %-25s |  %-25s |  %-25s\n", "No.", "TITLE", "ARTISTS", "TOTAL PLAYS");
            while (resultSet.next()) {
                count++;
                int musicId = resultSet.getInt("musicId");
                String title = resultSet.getString("title");
                String artists = resultSet.getString("artists");
                int totalPlays = resultSet.getInt("totalPlays");

                musicIds.add(musicId);
                System.out.printf("  %-4s |  %-25s |  %-25s |  %-25s\n", count, title, artists, totalPlays);
            }
            System.out.println("----------------------------------------------------------------------------------\n");

            while (true) {
                System.out.print("Enter the number of the music to see its details (or 0 to return): ");
                int choice = scan.nextInt();
                scan.nextLine();

                if (choice == 0) return;

                if (choice > 0 && choice <= musicIds.size()) {
                    int selectedMusicId = musicIds.get(choice - 1);
                    userMusicFeatures(selectedMusicId);
                    break;
                } else {
                    System.out.println("Invalid choice! Try again...");
                }
            }
        }
    }

    // Most Liked Music
    private static void mostLikedMusic() throws SQLException {
        while (true) {
            String sql = "SELECT m.musicId, m.title, m.releaseDate, GROUP_CONCAT(DISTINCT a.artistName SEPARATOR ', ') AS artists, "
                    + "tl.totalLikes AS totalLikes "
                    + "FROM music m "
                    + "LEFT JOIN ("
                    + "SELECT musicId, COUNT(userId) AS totalLikes "
                    + "FROM likes GROUP BY musicId"
                    + ") AS tl ON tl.musicId = m.musicId "
                    + "LEFT JOIN artists a ON m.musicID = a.musicId "
                    + "GROUP BY m.musicId "
                    + "ORDER BY totalLikes DESC";
            resultSet = Security.sqlResult(sql);

            ArrayList<Integer> musicIds = new ArrayList<>();
            int count = 1;

            System.out.println("\n============================== Most Liked Music Chart ==============================");
            System.out.println("----------------------------------------------------------------------------------");
            System.out.printf(" %4s  |  %-25s |  %-25s |  %-25s\n", "No.", "TITLE", "ARTISTS", "TOTAL LIKES");
            while (resultSet.next()) {
                int musicId = resultSet.getInt("musicId");
                String title = resultSet.getString("title");
                String artists = resultSet.getString("artists");
                int totalLikes = resultSet.getInt("totalLikes");

                musicIds.add(musicId);
                System.out.printf("  %-4s |  %-25s |  %-25s |  %-25s\n", count, title, artists, totalLikes);
                count++;
            }
            System.out.println("----------------------------------------------------------------------------------\n");

            while (true) {
                System.out.print("Enter the number of the music to see its details (or 0 to return): ");
                int choice = scan.nextInt();
                scan.nextLine();

                if (choice == 0) return;

                if (choice > 0 && choice <= musicIds.size()) {
                    int selectedMusicId = musicIds.get(choice - 1);
                    userMusicFeatures(selectedMusicId);
                    break;
                } else {
                    System.out.println("Invalid choice! Try again...");
                }
            }
        }
    }

    /* User's Music Features */
    /* User can reach to this feature for every music in every page */
    private static void userMusicFeatures(int musicId) throws SQLException {
        String input;
        Manager.showMusicDetails(musicId);
        while (true) {
            String sql = "SELECT COUNT(*) FROM likes WHERE userId = " + currentUserId + " AND musicId = " + musicId;
            resultSet = Security.sqlResult(sql);
            resultSet.next();
            int isLiked = resultSet.getInt(1);

            System.out.println("----------------------------------------------------------------------------------");
            System.out.println("0. Return to previous menu");
            System.out.println("1. Play Music");
            if (isLiked == 1) {
                System.out.println("2. Unlike");
            } else if (isLiked == 0) {
                System.out.println("2. Like");
            }
            System.out.println("3. Add to Playlist");
            System.out.println("----------------------------------------------------------------------------------");
            System.out.print("Please enter your option: ");
            input = scan.nextLine();

            switch (input) {
                case "0":
                    return;
                case "1":
                    playMusic(musicId, isLiked);
                    break;
                case "2": {
                    if (isLiked == 1) { // delete likes
                        sql = "DELETE FROM likes WHERE userId = " + currentUserId + " AND musicId = " + musicId;
                        System.out.println("Unliked successfully!");
                    } else { // insert likes
                        sql = "INSERT INTO likes VALUES(" + currentUserId + ", " + musicId + ")";
                        System.out.println("Liked successfully!");
                    }
                    Security.sqlUpdate(sql);
                    break;
                }
                case "3":
                    addToPlaylist(musicId);
                    break;
                default:
                    System.out.println("Wrong Input! Try again...");
                    break;
            }
            System.out.println();
        }
    }

    private static void playMusic(int musicId, int isLiked) throws SQLException {
        String queryTitle = "SELECT title, lyrics FROM music WHERE musicId = " + musicId;
        resultSet = Security.sqlResult(queryTitle);
        resultSet.next();
        System.out.println("\n========= " + resultSet.getString("title") + " =========\n");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println(resultSet.getString("lyrics"));
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        System.out.println("00:30 ----o----------------------------");
        System.out.println("        <<            ||           >>  ");

        String queryLikes = "SELECT COUNT(userId) AS totalLikes FROM likes WHERE musicId = " + musicId;
        ResultSet rs1 = Security.sqlResult(queryLikes);
        String queryPlays = "SELECT IFNULL(SUM(numOfPlays), 0) AS totalPlays FROM plays WHERE musicId = " + musicId;
        ResultSet rs2 = Security.sqlResult(queryPlays);
        if (rs1.next() && rs2.next()) {
            int totalLikes = rs1.getInt("totalLikes");
            int totalPlays = rs2.wasNull() ? 0 : rs2.getInt("totalPlays");
            if (isLiked == 1) {
                System.out.printf("♥ " + totalLikes + " likes %23s plays", totalPlays);
            } else if (isLiked == 0) {
                System.out.printf("♡ " + totalLikes + " likes %23s plays", totalPlays);
            }
        }

        String queryCount = "SELECT COUNT(*) FROM plays WHERE musicId = " + musicId + " AND userId = " + currentUserId;
        resultSet = Security.sqlResult(queryCount);
        resultSet.next();
        String sql;
        if (resultSet.getInt(1) == 0) {
            sql = "INSERT INTO plays VALUES(" + currentUserId + ", " + musicId + ", 1)";
        } else {
            sql = "UPDATE plays SET numOfPlays = numOfPlays + 1 WHERE musicId = " + musicId + " AND userId = " + currentUserId;
        }
        Security.sqlUpdate(sql);
        System.out.println();
    }

    private static void addToPlaylist(int musicId) throws SQLException {
        System.out.println("\n\n============================== Add Music To Playlist =============================");
        String sql = "SELECT * FROM playlists WHERE ownedBy = " + currentUserId;
        resultSet = Security.sqlResult(sql);

        ArrayList<String> playlists = new ArrayList<>();
        int count = 0;
        String title;
        System.out.println("----------------------------------------------------------------------------------");
        System.out.printf(" %4s  |  %-25s \n", "No.", "PLAYLIST TITLE");
        while (resultSet.next()) {
            count++;
            title = resultSet.getString("title");
            playlists.add(title);
            System.out.printf("  %-4s |  %-25s \n", count, title);
        }
        String input;
        if (count == 0) {
            System.out.println("Playlist does not exist.");
            System.out.println("----------------------------------------------------------------------------------");
            System.out.println("0. Return to previous menu");
            System.out.println("1. Create Playlist & Add To Playlist");
            System.out.print("Enter your option: ");
            input = scan.nextLine();
            if (input.equals("0")) {
                return;
            } else if (input.equals("1")) {
                title = createPlaylist();
                sql = "INSERT INTO addMusic VALUES('" + title + "', " + currentUserId + ", " + musicId + ")";
                Security.sqlUpdate(sql);
                System.out.println("----------------------------------------------------------------------------------");
                System.out.println("Added to playlist successfully!");
            } else {
                System.out.println("Wrong input! Try again...");
                return;
            }
        }
        if (count > 0) {
            System.out.println("----------------------------------------------------------------------------------");
            while (true) {
                System.out.print("Enter the number of the playlist (or 0 to return): ");
                int choice = scan.nextInt();
                scan.nextLine();

                if (choice == 0) return;

                if (choice > 0 && choice <= playlists.size()) {
                    String selectedTitle = playlists.get(choice - 1);
                    sql = "SELECT COUNT(*) FROM addMusic WHERE playlistTitle = '" + selectedTitle + "'" + " AND playlistOwner = " + currentUserId + " AND musicId = " + musicId;
                    resultSet = Security.sqlResult(sql);
                    resultSet.next();
                    if (resultSet.getInt(1) == 1) {
                        System.out.println("Music already exists in playlist!");
                    } else {
                        sql = "INSERT INTO addMusic VALUES('" + selectedTitle + "', " + currentUserId + ", " + musicId + ")";
                        Security.sqlUpdate(sql);
                        System.out.println("Added to playlist successfully!");
                    }
                    break;
                } else {
                    System.out.println("Invalid choice! Try again...");
                }
            }
        }
    }

    /* Search for Music */
    static void searchForMusic() throws SQLException {
        while (true) {
            System.out.println("\n\n================================ Search For Music ================================");
            System.out.print("Enter a title keyword (or 0 to return): ");
            String keyword = scan.nextLine();
            if (keyword.equals("0")) {
                return;
            }

            String queryInfo = "SELECT m.musicId, m.title, m.releaseDate, GROUP_CONCAT(DISTINCT a.artistName SEPARATOR ', ') AS artists "
                    + "FROM music AS m "
                    + "LEFT JOIN artists a ON m.musicId = a.musicId "
                    + "WHERE LOWER(m.title) LIKE '%" + keyword.toLowerCase() + "%' " // make the search keyword case-insensitive
                    + "GROUP BY m.musicId "
                    + "ORDER BY m.title";
            resultSet = Security.sqlResult(queryInfo);

            ArrayList<Integer> musicIds = new ArrayList<>();
            int count = 0;
            System.out.println("\nSearch Results:");
            System.out.println("----------------------------------------------------------------------------------");
            System.out.printf(" %4s  |  %-25s |  %-25s |  %-25s\n", "No.", "TITLE", "ARTISTS", "RELEASE DATE");
            while (resultSet.next()) {
                int musicId = resultSet.getInt("musicId");
                String artists = resultSet.getString("artists");
                String releaseDate = resultSet.getString("releaseDate");
                String title = resultSet.getString("title");
                count++;
                musicIds.add(musicId);
                System.out.printf("  %-4s |  %-25s |  %-25s |  %-25s\n", count, title, artists, releaseDate);
            }
            if (count == 0) {
                System.out.println("No music found!");
                return;
            }

            System.out.println("----------------------------------------------------------------------------------\n");
            System.out.print("Enter the number of the music to see its details (or 0 to return): ");
            int choice = scan.nextInt();
            scan.nextLine();

            if (choice == 0) return;

            if (currentUserId != null && Manager.currentManagerId == null) {
                if (choice > 0 && choice <= musicIds.size()) {
                    int selectedMusicId = musicIds.get(choice - 1);
                    userMusicFeatures(selectedMusicId);
                } else {
                    System.out.println("Invalid choice! Try again...");
                }
            } else if (currentUserId == null && Manager.currentManagerId != null) {
                if (choice > 0 && choice <= musicIds.size()) {
                    int selectedMusicId = musicIds.get(choice - 1);
                    Manager.showMusicDetails(selectedMusicId);
                    String queryManaged = "SELECT COUNT(*) AS isManaged FROM music WHERE musicId = " + selectedMusicId + " AND managedBy = " + Manager.currentManagerId;
                    resultSet = Security.sqlResult(queryManaged);
                    resultSet.next();
                    int isManaged = resultSet.getInt("isManaged");
                    if (isManaged == 1) {
                        while (true) {
                            System.out.print("Do you want to edit this music (Y/N) ");
                            String edit = scan.nextLine();
                            if (edit.equalsIgnoreCase("N")) {
                                break;
                            } else if (edit.equalsIgnoreCase("Y")) {
                                Manager.editMusic(selectedMusicId);
                                break;
                            } else {
                                System.out.println("\nWrong Input! Try again...");
                            }
                        }
                    }
                } else {
                    System.out.println("Invalid choice! Try again...");
                }
            } else {
                System.out.println("Invalid choice! Try again...");
            }
        }
    }

    /* Albums */
    static void showAlbumDetails(int albumId) throws SQLException {
        while (true) {
            String query = "SELECT al.title AS albumTitle, al.releaseDate, m.musicId, m.title AS musicTitle, "
                    + "GROUP_CONCAT(DISTINCT a.artistName SEPARATOR ', ') AS artists "
                    + "FROM albums al "
                    + "LEFT JOIN music m ON al.albumId = m.albumId "
                    + "LEFT JOIN artists a ON m.musicId = a.musicId "
                    + "WHERE al.albumId = " + albumId
                    + " GROUP BY m.musicId "
                    + "ORDER BY m.title";
            resultSet = Security.sqlResult(query);

            String albumTitle = null;
            String releaseDate = null;
            ArrayList<Integer> musicIds = new ArrayList<>();
            int count = 0;
            while (resultSet.next()) {
                if (albumTitle == null && releaseDate == null) {
                    albumTitle = resultSet.getString("albumTitle");
                    releaseDate = resultSet.getString("releaseDate");
                    System.out.println("Album Title: " + albumTitle + " (Released on " + releaseDate + ")");
                    System.out.println("---------------------------------------------------------------");
                    System.out.printf(" %4s  |  %-25s |  %-25s \n", "No.", "MUSIC TITLE", "ARTISTS");
                }

                count++;
                int musicId = resultSet.getInt("musicId");
                String musicTitle = resultSet.getString("musicTitle");
                String artists = resultSet.getString("artists");

                musicIds.add(musicId);
                if (musicTitle == null) {
                    System.out.println("No Music Exists!");
                    System.out.println("---------------------------------------------------------------");
                    System.out.println("Press Enter to return.");
                    scan.nextLine();
                    return;
                } else {
                    System.out.printf("  %-4s |  %-25s |  %-25s \n", count, musicTitle, artists);
                }
            }
            System.out.println("---------------------------------------------------------------");
            while (true) {
                System.out.print("\nEnter the number of the music to see its details (or 0 to return): ");
                int choice = scan.nextInt();
                scan.nextLine();

                if (choice == 0) return;

                if (choice > 0 && choice <= musicIds.size()) {
                    int selectedMusicId = musicIds.get(choice - 1);
                    userMusicFeatures(selectedMusicId);
                    break;
                } else {
                    System.out.println("Invalid choice! Try again...");
                }
            }
        }
    }

    /* Playlist */
    private static void playlistMenu() throws SQLException {
        String input;

        while (true) {
            System.out.println("\n\n=================================== Playlists ====================================");
            System.out.println("0. Return to previous menu");
            System.out.println("1. My Playlists"); // edit playlist / delete(playlist/music)
            System.out.println("2. Show Public Playlists");
            System.out.println("3. Search for Playlist"); // show other's playlists & search & play music
            System.out.println("4. Create Playlist");
            System.out.println("----------------------------------------------------------------------------------");
            System.out.print("Please enter your option: ");
            input = scan.nextLine();

            switch (input) {
                case "0": {
                    return;
                }
                case "1": {
                    myPlaylistMenu();
                    break;
                }
                case "2": {
                    showPublicPlaylists();
                    break;
                }
                case "3": {
                    searchForPlaylist();
                    break;
                }
                case "4": {
                    createPlaylist();
                    System.out.println("----------------------------------------------------------------------------------");
                    System.out.println("Created playlist successfully!");
                    break;
                }
                default:
                    break;
            }
        }
    }

    private static void myPlaylistMenu() throws SQLException {
        while (true) {
            String sql = "SELECT p.title AS playlistTitle, p.isShared, m.musicId, m.title AS musicTitle, "
                    + "GROUP_CONCAT(DISTINCT a.artistName SEPARATOR ', ') AS artists "
                    + "FROM playlists p LEFT JOIN addMusic am ON p.ownedBy = am.playlistOwner AND p.title = am.playlistTitle "
                    + "LEFT JOIN music m ON am.musicId = m.musicId "
                    + "LEFT JOIN artists a ON m.musicId = a.musicId "
                    + "WHERE p.ownedBy = " + currentUserId
                    + " GROUP BY p.title, m.musicId "
                    + "ORDER BY p.title";
            resultSet = Security.sqlResult(sql);

            System.out.println("\n\n=================================== My Playlists ===================================");
            String currentPlaylist = "";
            int count = 0;
            ArrayList<String> playlistIds = new ArrayList<>();
            Map<String, List<String>> playlistMusicTitles = new HashMap<>();
            Map<String, List<String>> playlistArtists = new HashMap<>();

            while (resultSet.next()) {
                String playlistTitle = resultSet.getString("playlistTitle");
                boolean isShared = resultSet.getBoolean("isShared");
                String musicTitle = resultSet.getString("musicTitle");
                String artists = resultSet.getString("artists");

                if (!playlistTitle.equals(currentPlaylist)) {
                    if (count != 0) {
                        System.out.println("---------------------------------------------------------------");
                    }
                    if (!currentPlaylist.equals("")) {
                        System.out.println();
                    }
                    count++;
                    System.out.println(count + ". Playlist: " + playlistTitle + (isShared ? " (Public)" : " (Private)"));
                    System.out.println("---------------------------------------------------------------");
                    System.out.printf(" %-25s  |  %-25s \n", "MUSIC TITLE", "ARTISTS");
                    currentPlaylist = playlistTitle;
                    playlistIds.add(playlistTitle);

                    playlistMusicTitles.put(playlistTitle, new ArrayList<>());
                    playlistArtists.put(playlistTitle, new ArrayList<>());
                }

                if (musicTitle != null) {
                    System.out.printf(" %-25s  |  %-25s \n", musicTitle, (artists != null ? artists : "Unknown Artist"));
                    playlistMusicTitles.get(playlistTitle).add(musicTitle);
                    playlistArtists.get(playlistTitle).add(artists != null ? artists : "Unknown Artist");
                } else {
                    System.out.println("No Music Found");
                }
            }
            System.out.println("---------------------------------------------------------------");

            if (count == 0) {
                System.out.println("No Playlists Found!");
                return;
            }

            System.out.println();

            while (true) {
                System.out.print("Enter the number of the playlist to manage (or 0 to return): ");
                int choice = scan.nextInt();
                scan.nextLine();

                if (choice == 0) return;

                if (choice > 0 && choice <= playlistIds.size()) {
                    String selectedPlaylistTitle = playlistIds.get(choice - 1);
                    List<String> musicList = playlistMusicTitles.get(selectedPlaylistTitle);
                    List<String> artistList = playlistArtists.get(selectedPlaylistTitle);
                    manageMyPlaylist(selectedPlaylistTitle, musicList, artistList);
                    break;
                } else {
                    System.out.println("Invalid choice! Try again...");
                }
            }
        }
    }

    private static int manageMyPlaylist(String playlistTitle, List<String> musicList, List<String> artistList) throws SQLException {
        System.out.println("0. Return to previous menu");
        System.out.println("1. Edit Playlist");
        System.out.println("2. Delete Playlist");
        System.out.println("3. Add Music To Playlist");
        System.out.println("4. Remove Music From Playlist");
        System.out.println("----------------------------------------------------------------------------------");
        System.out.print("Enter your option: ");
        String input = scan.nextLine();

        if (input.equals("0")) return 0;
        if (input.equals("1")) {
            editPlaylist(playlistTitle);
        } else if (input.equals("2")) {
            deletePlaylist(playlistTitle);
        } else if (input.equals("3")) {
            addMusicToPlaylist(playlistTitle);
        } else if (input.equals("4")) {
            deleteMusicFromPlaylist(playlistTitle, musicList, artistList);
        } else {
            System.out.println("Invalid input! Try again...");
        }
        System.out.println("----------------------------------------------------------------------------------");
        return 1;
    }

    private static void editPlaylist(String playlistTitle) throws SQLException {
        String sql = "SELECT * FROM playlists WHERE title = '" + playlistTitle + "' AND ownedBy = " + currentUserId;
        resultSet = Security.sqlResult(sql);
        String title = null;
        boolean isShared = false;

        while (resultSet.next()) {
            title = resultSet.getString("title");
            isShared = resultSet.getBoolean("isShared");
        }

        if (title == null) {
            System.out.println("No Playlist Found!");
            return;
        }

        System.out.println("----------------------------------------------------------------------------------");
        System.out.println("\nCurrent Playlist Details:");
        System.out.println("---------------------------------------------------------------");
        System.out.printf(" %-25s  |  %-25s \n", "TITLE", "SHARED STATUS");
        System.out.printf(" %-25s  |  %-25s \n", title, isShared ? "Public" : "Private");
        System.out.println("---------------------------------------------------------------");
        System.out.println();
        System.out.println("Press Enter to keep current value.");

        String newTitle;
        while (true) {
            System.out.print("Enter new title: ");
            newTitle = scan.nextLine();
            // playlists with the same userId cannot have the same title
            String checkTitleSql = "SELECT COUNT(*) FROM playlists WHERE title = '" + newTitle + "' AND ownedBy = " + currentUserId;
            resultSet = Security.sqlResult(checkTitleSql);
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                if (count > 0) {
                    System.out.println("Playlist with this title already exists. Choose a different title.");
                    continue;
                }
            }
            break;
        }
        String newSharedStatus;
        while (true) {
            System.out.print("Enter new shared status (private/public): ");
            newSharedStatus = scan.nextLine();
            if (newSharedStatus.equalsIgnoreCase("private")) {
                newSharedStatus = "false";
                break;
            } else if (newSharedStatus.equalsIgnoreCase("public")) {
                newSharedStatus = "true";
                break;
            } else if (!newSharedStatus.isEmpty()) {
                System.out.println("Invalid shared status! Try again.");
            } else {
                break;
            }
        }
        String finalTitle = newTitle.isEmpty() ? title : newTitle;
        boolean finalIsShared = newSharedStatus.isEmpty() ? isShared : Boolean.parseBoolean(newSharedStatus);

        String updatePlaylistQuery = "UPDATE addMusic SET playlistTitle = '" + finalTitle + "' WHERE playlistTitle = '" + title + "' AND playlistOwner = " + currentUserId;
        Security.sqlUpdate(updatePlaylistQuery);
        updatePlaylistQuery = "UPDATE playlists SET title = '" + finalTitle + "', isShared = " + finalIsShared + " WHERE title = '" + title + "' AND ownedBy = " + currentUserId;
        Security.sqlUpdate(updatePlaylistQuery);
        System.out.println("Playlist information updated successfully!");
    }

    private static void deletePlaylist(String playlistTitle) throws SQLException {
        System.out.print("Do you really want to delete this playlist? (Y/N) ");
        String delete = scan.nextLine();
        if (delete.equalsIgnoreCase("N")) return;
        else if (delete.equalsIgnoreCase("Y")) {
            String sql = "DELETE FROM addMusic WHERE playlistTitle = '" + playlistTitle + "' AND playlistOwner = " + currentUserId;
            Security.sqlUpdate(sql);
            sql = "DELETE FROM playlists WHERE title = '" + playlistTitle + "' AND ownedBy = " + currentUserId;
            Security.sqlUpdate(sql);
            System.out.println("Playlist deleted successfully!");
        } else {
            System.out.println("Invalid input! Try again.");
        }
    }

    private static void addMusicToPlaylist(String playlistTitle) throws SQLException {
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

            // Check if the music already exists
            String queryCheck = "SELECT m.musicId, m.title, m.releaseDate, GROUP_CONCAT(DISTINCT a.artistName SEPARATOR ', ') AS artists "
                    + "FROM music m "
                    + "LEFT JOIN artists a ON m.musicId = a.musicId "
                    + "WHERE m.title = '" + musicTitle + "' AND m.releaseDate = '" + releaseDate + "' "
                    + "GROUP BY m.musicId";
            resultSet = Security.sqlResult(queryCheck);

            if (resultSet.next()) {
                int musicId = resultSet.getInt("musicId");
                String artistInDB = resultSet.getString("artists");

                // validate if the artist matches
                if (artistInDB == null || !artistInDB.equalsIgnoreCase(artists.trim())) {
                    System.out.println("No such music found. Try again.");
                } else {
                    queryCheck = "SELECT COUNT(*) FROM addMusic WHERE playlistTitle = '" + playlistTitle + "' AND playlistOwner = " + currentUserId + " AND musicId = " + musicId;
                    resultSet = Security.sqlResult(queryCheck);
                    if (resultSet.next()) {
                        if (resultSet.getInt(1) > 0) {
                            System.out.println("Music already exists in this album.");
                            return;
                        }
                    }
                    // insert music into playlist
                    String addMusicToPlaylistQuery = "INSERT INTO addMusic (playlistTitle, playlistOwner, musicId) VALUES ('" + playlistTitle + "', " + currentUserId + ", " + musicId + ")";
                    Security.sqlUpdate(addMusicToPlaylistQuery);
                    System.out.println("Music added to playlist successfully!");
                    return;
                }
            } else {
                System.out.println("No such music found. Try again.");
            }
        }
    }

    private static void deleteMusicFromPlaylist(String playlistTitle, List<String> musicList, List<String> artistList) throws SQLException {
        System.out.println("\nMusic List in Playlist: ");
        System.out.println("----------------------------------------------------------------------------------");
        System.out.printf(" %4s  |  %-25s |  %-25s \n", "No.", "TITLE", "ARTISTS");
        int count2 = 0;
        for (int i = 0; i < musicList.size(); i++) {
            count2++;
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
                        String queryDelete = "DELETE FROM addMusic WHERE playlistTitle = '" + playlistTitle + "' AND playlistOwner = " + currentUserId + " AND musicId = " + musicId;
                        Security.sqlUpdate(queryDelete);
                        System.out.println("Music deleted from playlist successfully!");
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

    private static void showPublicPlaylists() throws SQLException {
        while (true) {
            System.out.println("\n\n============================== Show Public Playlists =============================");
            String query = "SELECT p.title AS playlistTitle, p.ownedBy, u.username, m.musicId, m.title AS musicTitle, m.releaseDate, GROUP_CONCAT(DISTINCT a.artistName SEPARATOR ', ') AS artists "
                    + "FROM playlists p "
                    + "JOIN users u ON p.ownedBy = u.userId "
                    + "LEFT JOIN addMusic am ON p.title = am.playlistTitle AND p.ownedBy = am.playlistOwner "
                    + "LEFT JOIN music m ON am.musicId = m.musicId "
                    + "LEFT JOIN artists a ON m.musicId = a.musicId "
                    + "WHERE p.isShared = true "
                    + "GROUP BY p.title, u.username, m.musicId, m.title, m.releaseDate "
                    + "ORDER BY p.title";
            resultSet = Security.sqlResult(query);

            ArrayList<Integer> playlistIds = new ArrayList<>();
            ArrayList<String> playlistTitles = new ArrayList<>();
            int count = 0;
            String currentPlaylist = "";

            while (resultSet.next()) {
                String playlistTitle = resultSet.getString("playlistTitle");
                String ownerName = resultSet.getString("username");
                String musicTitle = resultSet.getString("musicTitle");
                String releaseDate = resultSet.getString("releaseDate");
                String artists = resultSet.getString("artists");
                int owner = resultSet.getInt("ownedBy");

                if (!playlistTitle.equals(currentPlaylist)) {
                    if (count != 0) {
                        System.out.println("----------------------------------------------------------------------------------");
                    }
                    if (!currentPlaylist.isEmpty()) {
                        System.out.println();
                    }
                    count++;
                    System.out.println(count + ". Playlist: " + playlistTitle + " (made by '" + ownerName + "')");
                    System.out.println("----------------------------------------------------------------------------------");
                    System.out.printf(" %-25s  |  %-25s |  %-25s\n", "MUSIC TITLE", "ARTISTS", "RELEASE DATE");

                    currentPlaylist = playlistTitle;
                    playlistTitles.add(playlistTitle);
                    playlistIds.add(owner);
                }

                if (musicTitle != null) {
                    System.out.printf(" %-25s  |  %-25s |  %-25s \n", musicTitle, (artists != null ? artists : "Unknown Artist"), releaseDate);
                } else {
                    System.out.println("No Music Found");
                }
            }
            System.out.println("----------------------------------------------------------------------------------");
            if (count == 0) {
                System.out.println("No public playlists found.\n");
                continue;
            }

            while (true) {
                System.out.print("\nEnter the number of the playlist to see its details (or 0 to return): ");
                int choice = scan.nextInt();
                scan.nextLine();

                if (choice == 0) return;

                if (choice > 0 && choice <= playlistIds.size()) {
                    String selectedPlaylistTitle = playlistTitles.get(choice - 1);
                    int selectedPlaylistId = playlistIds.get(choice - 1);
                    playlistDetails(selectedPlaylistTitle, selectedPlaylistId);
                    break;
                } else {
                    System.out.println("Invalid choice! Try again...");
                }
            }
        }
    }

    private static void playlistDetails(String playlistTitle, int ownerId) throws SQLException {
        while (true) {
            String sql = "SELECT p.title AS playlistTitle, p.isShared, m.musicId, m.title AS musicTitle, p.ownedBy, "
                    + "GROUP_CONCAT(DISTINCT a.artistName SEPARATOR ', ') AS artists "
                    + "FROM playlists p "
                    + "LEFT JOIN addMusic am ON p.ownedBy = am.playlistOwner AND p.title = am.playlistTitle "
                    + "LEFT JOIN music m ON am.musicId = m.musicId "
                    + "LEFT JOIN artists a ON m.musicId = a.musicId "
                    + "WHERE p.ownedBy = " + ownerId + " AND p.title = '" + playlistTitle + "' "
                    + "GROUP BY p.title, m.musicId "
                    + "ORDER BY p.title";
            resultSet = Security.sqlResult(sql);

            System.out.println("----------------------------------------------------------------------------------");
            System.out.println(playlistTitle + ": ");
            int count = 0;
            int ownedBy = -1;
            ArrayList<Integer> musicIds = new ArrayList<>();
            List<String> musicTitles = new ArrayList<>();
            List<String> artistNames = new ArrayList<>();
            System.out.println("----------------------------------------------------------------------------------");
            System.out.printf(" %4s  |  %-25s |  %-25s \n", "No.", "MUSIC TITLE", "ARTISTS");
            String musicTitle = null;
            while (resultSet.next()) {
                int musicId = resultSet.getInt("musicId");
                musicTitle = resultSet.getString("musicTitle");
                boolean isShared = resultSet.getBoolean("isShared");
                String artists = resultSet.getString("artists");
                ownedBy = resultSet.getInt("ownedBy");

                count++;
                if (count == 1 && musicTitle == null) {
                    System.out.println("No Music Found\n");
                } else {
                    System.out.printf("  %-4s |  %-25s |  %-25s \n", count, musicTitle, artists);
                    musicIds.add(musicId);
                    musicTitles.add(musicTitle);
                    artistNames.add(artists);
                }
            }
            if (count == 0) {
                System.out.println("No Music Found\n");
            }
            System.out.println("----------------------------------------------------------------------------------");

            if (ownedBy == currentUserId) {
                int flag = manageMyPlaylist(playlistTitle, musicTitles, artistNames);
                if (flag == 0) {
                    break;
                }
            } else {
                if (count == 0 || (count == 1 && musicTitle == null)) {
                    break;
                }
                System.out.print("Enter the number of music to see its details (or 0 to return): ");
                int choice = scan.nextInt();
                if (choice == 0) return;

                if (choice > 0 && choice <= musicIds.size()) {
                    int selectedMusicId = musicIds.get(choice - 1);
                    userMusicFeatures(selectedMusicId);
                    break;
                } else {
                    System.out.println("Invalid choice! Try again...");
                }
            }
        }
    }

    private static void searchForPlaylist() throws SQLException {
        while (true) {
            System.out.println("\n\n============================== Search For Playlist ===============================");
            System.out.print("Enter a title keyword (or 0 to return): ");
            String keyword = scan.nextLine();
            if (keyword.equals("0")) {
                break;
            }

            String querySearch = "SELECT p.title AS playlistTitle, p.ownedBy, u.username, m.musicId, m.title AS musicTitle, m.releaseDate, GROUP_CONCAT(DISTINCT a.artistName SEPARATOR ', ') AS artists "
                    + "FROM playlists p "
                    + "JOIN users u ON p.ownedBy = u.userId "
                    + "LEFT JOIN addMusic am ON p.title = am.playlistTitle AND p.ownedBy = am.playlistOwner "
                    + "LEFT JOIN music m ON am.musicId = m.musicId "
                    + "LEFT JOIN artists a ON m.musicId = a.musicId "
                    + "WHERE LOWER(p.title) LIKE '%" + keyword.toLowerCase() + "%' AND p.isShared = true " // make the search keyword case-insensitive
                    + "GROUP BY p.title, u.username, m.musicId, m.title, m.releaseDate "
                    + "ORDER BY p.title"; // search for titles including the keyword
            resultSet = Security.sqlResult(querySearch);

            ArrayList<Integer> playlistIds = new ArrayList<>();
            ArrayList<String> playlistTitles = new ArrayList<>();
            int count = 0;
            String currentPlaylist = "";

            System.out.println("\nSearch Results for \"" + keyword + "\": ");
            while (resultSet.next()) {
                String playlistTitle = resultSet.getString("playlistTitle");
                String ownerName = resultSet.getString("username");
                String musicTitle = resultSet.getString("musicTitle");
                String releaseDate = resultSet.getString("releaseDate");
                String artists = resultSet.getString("artists");
                int owner = resultSet.getInt("ownedBy");

                if (!playlistTitle.equals(currentPlaylist)) {
                    count++;
                    if (!currentPlaylist.isEmpty()) {
                        System.out.println("----------------------------------------------------------------------------------");
                    }
                    System.out.println(count + ". Playlist: " + playlistTitle + " (made by '" + ownerName + "')");
                    System.out.println("----------------------------------------------------------------------------------");
                    System.out.printf(" %-25s  |  %-25s |  %-25s\n", "MUSIC TITLE", "ARTISTS", "RELEASE DATE");
                    currentPlaylist = playlistTitle;
                    playlistIds.add(owner);
                    playlistTitles.add(playlistTitle);
                }

                if (musicTitle != null) {
                    System.out.printf(" %-25s  |  %-25s |  %-25s \n", musicTitle, (artists != null ? artists : "Unknown Artist"), releaseDate);
                } else {
                    System.out.println("No Music Found");
                }
            }
            if (count == 0) {
                System.out.println("No playlists found matching \"" + keyword + "\"\n");
                continue;
            }
            System.out.println("----------------------------------------------------------------------------------");

            while (true) {
                System.out.print("\nEnter the number of the playlist to see its details (or 0 to return): ");
                int choice = scan.nextInt();
                scan.nextLine();

                if (choice == 0) return;

                if (choice > 0 && choice <= playlistIds.size()) {
                    int selectedPlaylistId = playlistIds.get(choice - 1);
                    String selectedPlaylistTitle = playlistTitles.get(choice - 1);
                    playlistDetails(selectedPlaylistTitle, selectedPlaylistId);
                    break;
                } else {
                    System.out.println("Invalid choice! Try again...");
                }
            }
        }
    }

    private static String createPlaylist() throws SQLException {
        System.out.println("\n\n================================ Create Playlist =================================");

        String title;
        while (true) {
            System.out.print("Enter playlist title: ");
            title = scan.nextLine();
            if (title.isEmpty()) {
                System.out.println("Title is required. Try again.");
                continue;
            }
            String checkTitleSql = "SELECT COUNT(*) FROM playlists WHERE title = '" + title + "' AND ownedBy = " + currentUserId;
            resultSet = Security.sqlResult(checkTitleSql);
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                if (count > 0) {
                    System.out.println("Playlist with this title already exists. Choose a different title.");
                    continue; // Prompt the user to enter a different title
                }
            }
            break;
        }
        int isShared;
        while (true) {
            System.out.print("Is it \"private\"(only for you) or \"public\"(shared to others)? ");
            String isPrivate = scan.nextLine();
            if (isPrivate.equals("private")) {
                isShared = 0;
                break;
            } else if (isPrivate.equals("public")) {
                isShared = 1;
                break;
            } else {
                System.out.println("Please enter \"private\" or \"public\" only.");
            }
        }
        String sql = "INSERT INTO playlists values('" + title + "', " + currentUserId + ", " + isShared + ")";
        Security.sqlUpdate(sql);
        return title;
    }

    /* Liked Music */
    private void likedMusic() throws SQLException {
        while (true) {
            System.out.println("\n\n=================================== Liked Music ==================================");
            String sql = "SELECT m.musicId, m.title, m.releaseDate, GROUP_CONCAT(DISTINCT a.artistName SEPARATOR ', ') AS artists "
                    + "FROM likes l "
                    + "JOIN music m ON l.musicId = m.musicId "
                    + "LEFT JOIN artists a ON m.musicId = a.musicId "
                    + "WHERE l.userId = " + currentUserId
                    + " GROUP BY m.musicId "
                    + "ORDER BY m.title";
            resultSet = Security.sqlResult(sql);

            int count = 0;
            ArrayList<Integer> musicIds = new ArrayList<>();
            System.out.println("----------------------------------------------------------------------------------");
            System.out.printf(" %4s  |  %-25s |  %-25s |  %-25s\n", "No.", "TITLE", "ARTISTS", "RELEASE DATE");
            while (resultSet.next()) {
                count++;
                musicIds.add(resultSet.getInt("musicId"));
                String musicId = resultSet.getString("musicId");
                String title = resultSet.getString("title");
                String releaseDate = resultSet.getString("releaseDate");
                String artists = resultSet.getString("artists");

                System.out.printf("  %-4s |  %-25s |  %-25s |  %-25s\n", count, title, artists, releaseDate);
            }
            if (count == 0) {
                System.out.println("No liked music found!");
                return;
            }

            while (true) {
                System.out.println("----------------------------------------------------------------------------------");
                System.out.print("\nEnter the number of the music to see its details (or 0 to return): ");
                int choice = scan.nextInt();
                scan.nextLine();

                if (choice == 0) return;

                if (choice > 0 && choice <= musicIds.size()) {
                    int selectedMusicId = musicIds.get(choice - 1);
                    userMusicFeatures(selectedMusicId);
                    break;
                } else {
                    System.out.println("Invalid choice! Try again...");
                }
            }
        }
    }

    /* My Profile */
    static void myProfile(String role) throws SQLException { // for both Manager & User
        while (true) {
            System.out.println("\n\n=================================== My Profile ===================================");
            String roleId = role.equals("users") ? "userId" : "managerId";
            int currentId = role.equals("users") ? currentUserId : Manager.currentManagerId;
            String sql = "SELECT username FROM " + role + " WHERE " + roleId + " = " + currentId;
            resultSet = Security.sqlResult(sql);
            resultSet.next();
            String username = resultSet.getString("username");
            System.out.println("My Username: " + username);
            System.out.println("----------------------------------------------------------------------------------");
            System.out.println("0. Return to previous menu");
            System.out.println("1. Edit Username");
            System.out.println("2. Edit Password");
            System.out.print("Enter your choice: ");
            String choice = scan.nextLine();
            if (choice.equals("0")) return;
            if (choice.equals("1")) {
                System.out.print("Enter new username: ");
                String newUsername = scan.nextLine();
                if (newUsername.isEmpty()) {
                    System.out.println("Username is required. Try again.");
                    continue;
                }
                String queryCheck = "SELECT COUNT(*) FROM " + role + " WHERE username = '" + newUsername + "'";
                resultSet = Security.sqlResult(queryCheck);
                resultSet.next();
                int count = resultSet.getInt("COUNT(*)");

                if (count > 0 && !newUsername.equals(username)) {
                    System.out.println("'" + newUsername + "' is already taken! Please choose a different username.");
                } else {
                    String queryUpdate = "UPDATE " + role + " SET username = '" + newUsername + "' WHERE " + roleId + " = " + currentId;
                    Security.sqlUpdate(queryUpdate);
                    System.out.println("Username updated successfully!");
                }
            } else if (choice.equals("2")) {
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
                String hashedPassword = Security.hashPassword(newPassword);
                String updateQuery = "UPDATE " + role + " SET password = '" + hashedPassword + "' WHERE " + roleId + " = " + currentId;
                Security.sqlUpdate(updateQuery);
                System.out.println("Password reset successfully!");
            } else {
                System.out.println("Invalid choice! Try again...");
            }
        }
    }

    /* Sign out */
    static void deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM plays WHERE userId = " + userId;
        Security.sqlUpdate(sql);
        sql = "DELETE FROM likes WHERE userId = " + userId;
        Security.sqlUpdate(sql);
        sql = "DELETE FROM addMusic WHERE playlistOwner = " + userId;
        Security.sqlUpdate(sql);
        sql = "DELETE FROM playlists WHERE ownedBy = " + userId;
        Security.sqlUpdate(sql);
        sql = "DELETE FROM users WHERE userId = " + userId;
        Security.sqlUpdate(sql);
    }
}
