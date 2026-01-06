package com.example.mama.meds;

import java.util.List;

public class DrugResponse {
    public List<Result> results;

    public static class Result {
        public OpenFda openfda;
    }

    public static class OpenFda {
        public List<String> generic_name;
        public List<String> brand_name;
    }
}