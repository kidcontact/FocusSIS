package com.slensky.focussis.data.domains;

import java.util.HashMap;
import java.util.Map;

public class GradSubjectDomain implements Domain<GradSubjectDomain.GradSubjectDomainKey, Map<String, GradSubjectDomain.GradSubject>> {
    public static final class GradSubject {
        private final String id;
        private final String shortName;
        private final String title;

        public GradSubject(String id, String shortName, String title) {
            this.id = id;
            this.shortName = shortName;
            this.title = title;
        }

        public String getId() {
            return id;
        }

        public String getShortName() {
            return shortName;
        }

        public String getTitle() {
            return title;
        }

    }
    public static final class GradSubjectDomainKey {
        private final String sYear;

        public GradSubjectDomainKey(String sYear) {
            this.sYear = sYear;
        }

        public String getsYear() {
            return sYear;
        }

    }

    private final Map<GradSubjectDomainKey, Map<String, GradSubject>> members;

    public GradSubjectDomain() {
        members = new HashMap<>();
    }

    public GradSubjectDomain(Map<GradSubjectDomainKey, Map<String, GradSubject>> members) {
        this.members = members;
    }

    public Map<GradSubjectDomainKey, Map<String, GradSubject>> getMembers() {
        return members;
    }

    @Override
    public void addMember(GradSubjectDomainKey key, Map<String, GradSubject> member) {
        members.put(key, member);
    }

}
