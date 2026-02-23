package com.back.domain.welfare.policy.search;

import org.springframework.stereotype.Component;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;

@Component
public class PolicyQueryBuilder {

    public Query build(PolicySearchCondition condition) {

        return Query.of(q -> q.bool(b -> {

            // 1️⃣ 정책명 키워드 검색
            if (hasText(condition.getKeyword())) {
                b.must(m -> m.match(mm -> mm.field("plcyNm").query(condition.getKeyword())));
            }

            // 2️⃣ 나이 조건
            if (condition.getAge() != null) {
                b.filter(f -> f.range(r -> r.field("minAge").lte(JsonData.of(condition.getAge()))));
                b.filter(f -> f.range(r -> r.field("maxAge").gte(JsonData.of(condition.getAge()))));
            }

            // 3️⃣ 소득 조건
            if (condition.getEarn() != null) {
                b.filter(f -> f.range(r -> r.field("earnMin").lte(JsonData.of(condition.getEarn()))));
                b.filter(f -> f.range(r -> r.field("earnMax").gte(JsonData.of(condition.getEarn()))));
            }

            // 4️⃣ 지역 / 직업 / 학력
            termFilter(b, "regionCode", condition.getRegionCode());
            termFilter(b, "jobCode", condition.getJobCode());
            termFilter(b, "schoolCode", condition.getSchoolCode());
            termFilter(b, "marriageStatus", condition.getMarriageStatus());

            // 5️⃣ 태그
            if (condition.getKeywords() != null && !condition.getKeywords().isEmpty()) {
                b.should(s -> s.terms(t -> t.field("keywords")
                        .terms(v -> v.value(condition.getKeywords().stream()
                                .map(FieldValue::of)
                                .toList()))));
            }

            return b;
        }));
    }

    private void termFilter(BoolQuery.Builder b, String field, String value) {
        if (value != null) {
            b.filter(f -> f.term(t -> t.field(field).value(value)));
        }
    }

    private boolean hasText(String text) {
        return text != null && !text.isBlank();
    }
}
