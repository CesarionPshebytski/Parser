import com.csvreader.CsvWriter;
import org.jsoup.Jsoup;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;

public class parser {
    public static void main(String[] args) throws IOException {
        String url = "https://rozetka.com.ua/ua/photo/c80001/filter/";
        parsePages(url);
    }

    private static void parsePages(String url) {
        String pageURL;
        try {
//count of pages with goods
            int pagesCount = Integer.parseInt(Jsoup.connect(url).get().getElementsByClass("paginator-catalog-l-link").last().text());
            for (int q = 1; q <= pagesCount; q++) {
//parsing through catalogue pages
                pageURL = url + "page=" + q + "/";
                parseCategoryPage(pageURL);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException q) {
            parseCategoryPage(url);
        }
    }

    private static void parseCategoryPage(String pageURL) {
        try {
            Elements x = Jsoup.connect(pageURL).get().getElementsByClass("g-rating-reviews-link");
            for (Element i : x) {
                int reviewsCount;
//checking if reviewsCount != 0
                try {
                    reviewsCount = Integer.parseInt((i.getElementsByClass("g-rating-reviews").text()).split(" ")[0]);
                } catch (NumberFormatException z) {
                    reviewsCount = 0;
                }
//output
                String goodsURL = String.valueOf(i.getElementsByAttribute("href")).split("\"")[3];
                System.out.println(reviewsCount + " reviews from " + goodsURL);
//parsing through reviews
                if (reviewsCount != 0)
                    parseReviews(goodsURL);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void parseReviews(String url) {
        String pageURL;
        boolean firstOpen = true;
        try {
//count of pages of reviews
            int pagesCount = Integer.parseInt(Jsoup.connect(url).get().getElementsByClass("paginator-catalog-l-link").last().text());
            for (int q = 1; q <= pagesCount; q++) {
//parsing through reviews pages
                pageURL = url + "page=" + q + ";sort=helpful";
                parseReviewsPage(pageURL,firstOpen);
                firstOpen = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException q) {
            parseReviewsPage(url + "page=1;sort=helpful/", firstOpen);
        }
    }

    private static void parseReviewsPage(String goodsURL, boolean firstOpen) {
        try {
            CsvWriter csvOutput;
//checking if file is opened first time to overwrite it otherwise to append it
            if(firstOpen) {
                csvOutput = new CsvWriter(new FileWriter(goodsURL.split("/")[4] + ".csv"), ',');
//file headers
                csvOutput.write("Rating");
                csvOutput.write("Likes");
                csvOutput.write("Dislikes");
                csvOutput.write("Username");
                csvOutput.write("Review text");
                csvOutput.endRecord();
            }else
//opening filewriter 4 appending
                csvOutput = new CsvWriter(new FileWriter(goodsURL.split("/")[4] + ".csv", true), ',');
            Elements x = Jsoup.connect(goodsURL).get().getElementsByClass("pp-review-i");
            for (Element i : x) {
                String rating;
                try {
                    rating = (i.getElementsByClass("sprite g-rating-stars-i").toString().split("\"")[3]);
                }catch (IndexOutOfBoundsException xx){
                    rating = "0";
                }
//writing reviews 2 file
                String reviewLikesCount = (i.getElementsByClass("pp-review-vote-positive").get(0).text());
                String reviewDislikesCount = (i.getElementsByClass("pp-review-vote-negative").get(0).text());
                String reviewAuthorName = (i.getElementsByClass("pp-review-author-name").text());
                String reviewText = (i.getElementsByClass("pp-review-text-i").get(0).text());
                System.out.println(reviewAuthorName+": "+reviewText);
                csvOutput.write(rating);
                csvOutput.write(reviewLikesCount.equals(" ")?"0":reviewLikesCount);
                csvOutput.write(reviewDislikesCount.equals(" ")?"0":reviewDislikesCount);
                csvOutput.write(reviewAuthorName);
                csvOutput.write(reviewText);
                csvOutput.endRecord();
            }
            csvOutput.flush();
            csvOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}