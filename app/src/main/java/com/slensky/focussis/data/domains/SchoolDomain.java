package com.slensky.focussis.data.domains;

import java.util.List;
import java.util.Map;

public class SchoolDomain {
    public static class School {
        private final String schoolId;
        private final String title;

        public School(String schoolId, String title) {
            this.schoolId = schoolId;
            this.title = title;
        }

        public String getSchoolId() {
            return schoolId;
        }

        public String getTitle() {
            return title;
        }

    }

    private final Map<String, School> members;

    public SchoolDomain(Map<String, School> members) {
        this.members = members;
    }

    public Map<String, School> getMembers() {
        return members;
    }

}
