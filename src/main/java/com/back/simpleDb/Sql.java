package com.back.simpleDb;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Sql {
    private SimpleDb simpleDb;
    private StringBuilder sb;
    List<Object> params;

    public Sql(SimpleDb simpleDb) {
        this.simpleDb = simpleDb;
        this.sb = new StringBuilder();
        this.params = new ArrayList<>();
    }

    public Sql append(String sql, Object... params) {
        sb.append(sql).append(" ");
        Collections.addAll(this.params, params);

        return this;
    }

    public Sql appendIn(String sql, Object... params) {
        if (params == null || params.length == 0) {
            return this;
        }

        // 배열이나 리스트가 인자로 들어왔을 경우를 대비한 '펼치기' 로직
        Object[] actualParams;
        if (params.length == 1 && params[0] instanceof Object[]) {
            actualParams = (Object[]) params[0];
        } else if (params.length == 1 && params[0] instanceof List) {
            actualParams = ((List<?>) params[0]).toArray();
        } else {
            actualParams = params;
        }

        int count = actualParams.length;

        // Stream을 사용하여 ?, ?, ? 형태의 문자열 생성
        String placeholders = Stream.generate(() -> "?")
                .limit(count)
                .collect(Collectors.joining(", "));

        // SQL 문장에서의 ?를 위에서 만든 placeholders로 치환
        String replacedSql = sql.replace("?", placeholders);

        // 결과 저장
        sb.append(replacedSql).append(" ");
        Collections.addAll(this.params, actualParams);

        return this;
    }

    public long insert() {
        String sql = sb.toString().trim();
        Object[] paramsArr = params.toArray();

        return simpleDb.insert(sql, paramsArr);
    }

    public int update() {
        String sql = sb.toString().trim();
        Object[] paramsArr = params.toArray();

        return simpleDb.update(sql, paramsArr);
    }

    public int delete() {
        String sql = sb.toString().trim();
        Object[] paramsArr = params.toArray();

        return simpleDb.delete(sql, paramsArr);
    }

    public List<Map<String, Object>> selectRows() {
        String sql = sb.toString().trim();
        Object[] paramsArr = params.toArray();

        return simpleDb.selectRows(sql, paramsArr);
    }

    public <T> List<T> selectRows(Class<T> cls) {
        String sql = sb.toString().trim();
        Object[] paramsArr = params.toArray();

        return simpleDb.selectRows(cls, sql, paramsArr);
    }

    public Map<String, Object> selectRow() {
        String sql = sb.toString().trim();
        Object[] paramsArr = params.toArray();

        return simpleDb.selectRow(sql, paramsArr);
    }

    public <T> T selectRow(Class<T> cls) {
        String sql = sb.toString().trim();
        Object[] paramsArr = params.toArray();

        return simpleDb.selectRow(cls, sql, paramsArr);
    }

    public LocalDateTime selectDatetime() {
        String sql = sb.toString().trim();
        Object[] paramsArr = params.toArray();

        return simpleDb.selectDatetime(sql, paramsArr);
    }

    public Long selectLong() {
        String sql = sb.toString().trim();
        Object[] paramsArr = params.toArray();

        return simpleDb.selectLong(sql, paramsArr);
    }

    public String selectString() {
        String sql = sb.toString().trim();
        Object[] paramsArr = params.toArray();

        return simpleDb.selectString(sql, paramsArr);
    }

    public Boolean selectBoolean() {
        String sql = sb.toString().trim();
        Object[] paramsArr = params.toArray();

        return simpleDb.selectBoolean(sql, paramsArr);
    }

    public List<Long> selectLongs() {
        String sql = sb.toString().trim();
        Object[] paramsArr = params.toArray();

        return simpleDb.selectLongs(sql, paramsArr);
    }
}
