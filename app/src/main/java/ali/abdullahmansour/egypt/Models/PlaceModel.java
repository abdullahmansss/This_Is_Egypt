package ali.abdullahmansour.egypt.Models;

public class PlaceModel
{
    String imageurl,placetitle,placedescription;

    public PlaceModel() { }

    public PlaceModel(String imageurl, String placetitle, String placedescription)
    {
        this.imageurl = imageurl;
        this.placetitle = placetitle;
        this.placedescription = placedescription;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getPlacetitle() {
        return placetitle;
    }

    public void setPlacetitle(String placetitle) {
        this.placetitle = placetitle;
    }

    public String getPlacedescription() {
        return placedescription;
    }

    public void setPlacedescription(String placedescription) {
        this.placedescription = placedescription;
    }
}
