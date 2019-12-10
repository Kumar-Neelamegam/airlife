package at.jku.mobilecomputing.airlife.Adapters;

public class FavouriteListObject {
    public int getSno() {
        return Sno;
    }

    public void setSno(int sno) {
        Sno = sno;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getQualityValue() {
        return QualityValue;
    }

    public void setQualityValue(String qualityValue) {
        QualityValue = qualityValue;
    }

    public int getQualityScale() {
        return QualityScale;
    }

    public void setQualityScale(int qualityScale) {
        QualityScale = qualityScale;
    }

    int Sno;
    String Location;
    String LocationInfo;
    String QualityValue;
    int QualityScale;

    public String getLocationInfo() {
        return LocationInfo;
    }

    public void setLocationInfo(String locationInfo) {
        LocationInfo = locationInfo;
    }



}
