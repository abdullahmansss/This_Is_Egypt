package ali.abdullahmansour.egypt.Models;

public class UserData
{
    String title,email,hotline,specialty,category,address,image_url,facebook_link;

    public UserData() { }

    public UserData(String title, String email, String hotline, String specialty, String category, String address, String image_url, String facebook_link) {
        this.title = title;
        this.email = email;
        this.hotline = hotline;
        this.specialty = specialty;
        this.category = category;
        this.address = address;
        this.image_url = image_url;
        this.facebook_link = facebook_link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHotline() {
        return hotline;
    }

    public void setHotline(String hotline) {
        this.hotline = hotline;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getFacebook_link() {
        return facebook_link;
    }

    public void setFacebook_link(String facebook_link) {
        this.facebook_link = facebook_link;
    }
}
