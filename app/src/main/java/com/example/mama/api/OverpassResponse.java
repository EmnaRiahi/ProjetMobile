package com.example.mama.api;

import com.google.gson.annotations.SerializedName; // <--- C'est cet import qui manquait peut-être
import java.util.List;

public class OverpassResponse {
    public List<Element> elements;

    public static class Element {
        public long id;
        public double lat;
        public double lon;
        public Tags tags;
    }

    public static class Tags {
        public String name;
        public String phone;

        @SerializedName("addr:city")
        public String city;
    }
}