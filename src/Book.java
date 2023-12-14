



public class Book {
    private String title;
    private String imageUrl;

    public Book(String title, String imageUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
    }

    // Getter方法
    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}