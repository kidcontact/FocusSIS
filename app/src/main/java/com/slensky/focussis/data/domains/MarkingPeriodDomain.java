package com.slensky.focussis.data.domains;

import java.util.HashMap;
import java.util.Map;

public class MarkingPeriodDomain implements Domain<MarkingPeriodDomain.MarkingPeriodDomainKey, Map<String, MarkingPeriodDomain.MarkingPeriod>> {
    public static final class MarkingPeriod {
        private final String id;
        private final String title;

        public MarkingPeriod(String id, String title) {
            this.id = id;
            this.title = title;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

    }
    public static final class MarkingPeriodDomainKey {
        private final String schoolId;
        private final String sYear;

        public MarkingPeriodDomainKey(String schoolId, String sYear) {
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

    private final Map<MarkingPeriodDomainKey, Map<String, MarkingPeriod>> members;

    public MarkingPeriodDomain() {
        this.members = new HashMap<>();
    }

    public MarkingPeriodDomain(Map<MarkingPeriodDomainKey, Map<String, MarkingPeriod>> members) {
        this.members = members;
    }

    public Map<MarkingPeriodDomainKey, Map<String, MarkingPeriod>> getMembers() {
        return members;
    }

    @Override
    public void addMember(MarkingPeriodDomainKey key, Map<String, MarkingPeriod> member) {
        members.put(key, member);
    }

}
