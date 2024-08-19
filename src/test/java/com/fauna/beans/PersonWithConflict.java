package com.fauna.beans;


import com.fauna.annotation.FaunaField;


public class PersonWithConflict {

    public static class PersonWithDateConflict {

        @FaunaField(name = "@date")
        public String field = "not";
    }

    public static class PersonWithDocConflict {

        @FaunaField(name = "@doc")
        public String field = "not";
    }

    public static class PersonWithDoubleConflict {

        @FaunaField(name = "@double")
        public String field = "not";
    }

    public static class PersonWithIntConflict {

        @FaunaField(name = "@int")
        public String field = "not";
    }

    public static class PersonWithLongConflict {

        @FaunaField(name = "@long")
        public String field = "not";
    }

    public static class PersonWithModConflict {

        @FaunaField(name = "@mod")
        public String field = "not";
    }

    public static class PersonWithObjectConflict {

        @FaunaField(name = "@object")
        public String field = "not";
    }

    public static class PersonWithRefConflict {

        @FaunaField(name = "@ref")
        public String field = "not";
    }

    public static class PersonWithSetConflict {

        @FaunaField(name = "@set")
        public String field = "not";
    }

    public static class PersonWithTimeConflict {

        @FaunaField(name = "@time")
        public String field = "not";
    }

}
