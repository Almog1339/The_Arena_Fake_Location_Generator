package Classes;

import com.javadocmd.simplelatlng.LatLng;

public class User {
    private String email;
    private LatLng coordinates;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }

    public User(String email, LatLng coordinates) {
        this.email = email;
        this.coordinates = coordinates;
    }
}
