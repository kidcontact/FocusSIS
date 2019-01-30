package com.slensky.focussis.data.domains;

import java.util.HashMap;
import java.util.Map;

public class GradeScaleDomain implements Domain<GradeScaleDomain.GradeScaleDomainKey, Map<String, GradeScaleDomain.GradeScale>> {
    public static final class GradeScale {
        private final String id;
        private final String title;
        private final boolean isDefault;

        public GradeScale(String id, String title, boolean isDefault) {
            this.id = id;
            this.title = title;
            this.isDefault = isDefault;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public boolean isDefault() {
            return isDefault;
        }

    }
    public static final class GradeScaleDomainKey {
        private final String schoolId;
        private final String sYear;

        public GradeScaleDomainKey(String schoolId, String sYear) {
            this.schoolId = schoolId;
            this.sYear = sYear;
        }

        public String getSchoolId() {
            return schoolId;
        }

        public String getsYear() {
            return sYear;
        }

    }

    private final Map<GradeScaleDomainKey, Map<String, GradeScale>> members;

    public GradeScaleDomain() {
        this.members = new HashMap<>();
    }

    public GradeScaleDomain(Map<GradeScaleDomainKey, Map<String, GradeScale>> members) {
        this.members = members;
    }

    public Map<GradeScaleDomainKey, Map<String, GradeScale>> getMembers() {
        return members;
    }

    @Override
    public void addMember(GradeScaleDomainKey key, Map<String, GradeScale> member) {
        members.put(key, member);
    }

}
