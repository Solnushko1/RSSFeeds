package parsing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.Utils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

public class RssToDatabase {
    private static final String URL = "https://www.google.com/alerts/feeds/16223384089229707479/8822547868054799039";
    private static final long PERIOD = 60 * 60 * 1000 * 24; // Обновляет новости раз в 1 day
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/alert";
    private static final String DB_USERNAME = "postgres";
    private static final String DB_PASSWORD = "qwerty007";

    public static void main(String[] args) {
        Timer timer = new Timer();
        timer.schedule(new RssTask(), 0, PERIOD);
    }

    private static class RssTask extends TimerTask {
        /**
         * Запускает функцию и выполняет следующую фигню:
         * - Извлекает документ по заданному URL с помощью библиотеки Jsoup.
         * - Извлекает определенные элементы из документа.
         * - Сохраняет извлеченные новости в базе данных, если они еще не сохранены.
         * - Выводит извлеченную новостную информацию на консоль.
         *
         * @return void
         */
        @Override
        public void run() {
            try {
                Document doc = Jsoup.connect(URL).get();

                Elements entryElements = doc.select("entry");

                for (Element entryElement : entryElements) {
                    Element idElement = entryElement.selectFirst("id");
                    Element titleElement = entryElement.selectFirst("title");
                    Element linkElement = entryElement.selectFirst("link");
                    Element publishedElement = entryElement.selectFirst("published");
                    Element updatedElement = entryElement.selectFirst("updated");
                    Element contentElement = entryElement.selectFirst("content");

                    String id = idElement.text();
                    String title = Utils.removeUnwantedSymbols(titleElement.text());
                    String link = linkElement.attr("href");
                    String published = publishedElement.text();
                    String updated = updatedElement.text();
                    String content = Utils.removeUnwantedSymbols(contentElement.html());

                    if (!isNewsAlreadySaved(id)) {
                        saveNewsToDatabase(id, title, link, published, updated, content);
                    }

                    System.out.println("ID: " + id);
                    System.out.println("Title: " + title);
                    System.out.println("Link: " + link);
                    System.out.println("Published: " + published);
                    System.out.println("Updated: " + updated);
                    System.out.println("Content: " + content);
                    System.out.println();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /**
         * Проверяет, сохранена ли новость с указанным идентификатором в базе данных.
         *
         * @param id идентификатор проверяемой новости
         * @return true, если новость уже сохранена, false в противном случае
         */
        private boolean isNewsAlreadySaved(String id) {
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
                 PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM news WHERE id = ?")) {
                statement.setString(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        int count = resultSet.getInt(1);
                        return count > 0;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }
        /**
         * Сохраняет данные о новостях в базе данных.
         *
         * @param id - идентификатор новости
         * @param title - название новости
         * @param link - ссылка на новость
         * @param published - дата публикации новости
         * @param updated дата обновления новости
         * @param content содержание новости
         */
        private void saveNewsToDatabase(String id, String title, String link, String published, String updated, String content) {
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO news (id, title, link, published, updated, content) VALUES (?, ?, ?, ?, ?, ?)")) {
                statement.setString(1, id);
                statement.setString(2, title);
                statement.setString(3, link);
                statement.setString(4, published);
                statement.setString(5, updated);
                statement.setString(6, content);
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
