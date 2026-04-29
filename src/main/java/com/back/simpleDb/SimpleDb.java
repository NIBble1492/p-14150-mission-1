package com.back.simpleDb;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleDb {
    private final String host;
    private final String user;
    private final String password;
    private final String dbName;
    // 개발 모드 변수
    private boolean devMode = false;
    private String url;

    public SimpleDb(String host, String user, String password, String dbName) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.dbName = dbName;

        this.url = String.format("jdbc:mysql://%s:3306/%s", this.host, this.dbName);
    }

    // DB 연결을 전담하는 헬퍼 메서드
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public void setDevMode(boolean b) {
        this.devMode = b;
    }

    public Sql genSql() {
        return new Sql(this);
    }

    private void logSql(String sql) {
        if (devMode) {
            System.out.println("실행된 SQL : " + sql);
        }
    }

    public void run(String sql, Object... params) {
        //logSql(sql);

        // try (...) 안에 넣으면, 실행이 끝나거나 에러가 나서 튕겨 나갈 때 자바가 자동으로 connection과 statement를 닫아준다.
        try (Connection connection = getConnection();
            //Statement statement = connection.createStatement();
            PreparedStatement stmt = connection.prepareStatement(sql);
        ) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i+1, params[i]);
            }
            stmt.executeUpdate();
            //statement.execute(sql);
        } catch (SQLException e) {
            // 여기서 잡아서 '언체크 예외'로 변환해서 던짐
            throw new RuntimeException(e);
        }
    }


    // 삽입하기
    public long insert(String sql, Object... params) {
        logSql(sql);

        try (Connection connection = getConnection();
            // RETURN_GENERATED_KEYS 옵션 추가
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ) {
            // 파라마터 셋팅
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i+1, params[i]);
            }
            stmt.executeUpdate();

            // 생성된 키 가져오기
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return -1; // 여기까지 오면 안 되지만, 문법상 추가
    }

    // 수정하기
    public int update(String sql, Object... params) {
        logSql(sql);
        return executeUpdate(sql, params);
    }

    // 삭제하기
    public int delete(String sql, Object... params) {
        logSql(sql);
        return executeUpdate(sql, params);
    }

    // 수정하기, 삭제하기 통합된 로직
    private int executeUpdate(String sql, Object... params) {
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
        ) {
            // 파라마터 셋팅
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i+1, params[i]);
            }

            // executeUpdate()는 수정/삭제된 행의 개수(int)를 반환합니다.
            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // 전체 row 조회하기
    public List<Map<String, Object>> selectRows(String sql, Object... params) {
        logSql(sql);

        List<Map<String, Object>> maps = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
        ) {
            // 파라마터 셋팅
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i+1, params[i]);
            }

            // 결과 데이터와 설계도(MetaData)를 준비합니다.
            try (ResultSet rs = stmt.executeQuery()) {

                // 줄(Row) 단위로 반복합니다.
                while (rs.next()) {
                    maps.add(rowToMap(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return maps;
    }

    // 특정 row 조회하기
    public Map<String, Object> selectRow(String sql, Object... params) {
        logSql(sql);

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
        ) {
            // 파라마터 셋팅
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i+1, params[i]);
            }

            // 결과 데이터와 설계도(MetaData)를 준비합니다.
            try (ResultSet rs = stmt.executeQuery()) {

                // row가 있으면
                if (rs.next()) {
                    return rowToMap(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // 한 줄의 ResultSet을 Map으로 변환한다.
    private Map<String, Object> rowToMap(ResultSet rs) throws SQLException {
        Map<String, Object> row = new HashMap<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // 해당 줄의 칸(Column) 단위로 반복합니다.
        for (int i = 1; i <= columnCount; i++) {
            row.put(metaData.getColumnLabel(i), rs.getObject(i));
        }
        return row;
    }

    public LocalDateTime selectDatetime(String sql, Object... params) {
        logSql(sql);
        return selectOne(LocalDateTime.class, sql, params);
    }

    public Long selectLong(String sql, Object... params) {
        logSql(sql);
        return selectOne(Long.class, sql, params);
    }

    public List<Long> selectLongs(String sql, Object... params) {
        return selectList(Long.class, sql, params);
    }

    public String selectString(String sql, Object... params) {
        logSql(sql);
        return selectOne(String.class, sql, params);
    }

    public Boolean selectBoolean(String sql, Object... params) {
        logSql(sql);
        return selectOne(Boolean.class, sql, params);
    }

    // 단일 행, 단일 열의 결과값을 지정한 타입(cls)으로 반환하는 범용 조회 메서드
    public <T> T selectOne(Class<T> cls, String sql, Object... params) {
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
        ) {
            // 파라마터 셋팅
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i+1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {

                // row가 있으면
                if (rs.next()) {
                    if (cls == Boolean.class || cls == boolean.class) {
                        return (T) Boolean.valueOf(rs.getBoolean(1));
                    }
                    return cls.cast(rs.getObject(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    public <T> List<T> selectList(Class<T> cls, String sql, Object... params) {
        List<T> list = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
        ) {
            // 파라마터 셋팅
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i+1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                // 줄(Row) 단위로 반복합니다.
                while (rs.next()) {
                    if (cls == Boolean.class || cls == boolean.class) {
                        list.add((T) Boolean.valueOf(rs.getBoolean(1)));
                    } else {
                        list.add(cls.cast(rs.getObject(1)));
                    }

                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
}
