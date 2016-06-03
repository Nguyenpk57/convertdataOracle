package telsoft.query;

import telsoft.jdbc.OracleConnUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class QueryVer2 {
    public static void main(String[] args) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("src/telsoft/inputdata/conver2.txt"));
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                String part[] = line.split(",");
                Connection connection = null;
                PreparedStatement preparedStatementSelect = null;
                PreparedStatement preparedStatementUpdate = null;
                ResultSet resultSet = null;
                try {
                    connection = OracleConnUtils.getOracleConnection();

                    String strSQLselect = " SELECT ";
                    String updateSQL = " UPDATE " + part[0] + " SET ";

                    for (int i = 1; i < part.length; i++) {
                        strSQLselect += part[i] + ", ";
                        if (i == part.length - 1) {
                            updateSQL += part[i] + " = ?";
                            break;
                        }
                        updateSQL += part[i] + " = ?" + ", ";
                    }
                    strSQLselect += "rowid FROM " + part[0];
                    updateSQL += " WHERE rowid = ?";

                    preparedStatementSelect = connection.prepareStatement(strSQLselect);
                    preparedStatementUpdate = connection.prepareStatement(updateSQL);
                    resultSet = preparedStatementSelect.executeQuery();

                    while (resultSet.next()) {
                        List<String> value = new ArrayList<String>();
                        String row = resultSet.getString("rowid");
                        preparedStatementUpdate.setString(part.length, row);
                        for (int i = 0; i < part.length - 1; i++) {
                            value.add(convertToComposeUnicode(resultSet.getString(part[i + 1])));
                            System.out.println(+i + " :  " + value.get(i));
                            preparedStatementUpdate.setString(i + 1, value.get(i));
                        }
                        preparedStatementUpdate.addBatch();
                    }
                    preparedStatementUpdate.executeBatch();

                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    close(connection);
                    close(preparedStatementSelect);
                    close(preparedStatementUpdate);
                    close(resultSet);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("No Exception");
        }
    }

    public static String convertToComposeUnicode(String composeUnicode) {
        return Normalizer.normalize(composeUnicode, Normalizer.Form.NFD);
    }

    public static void close(Connection connection) throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    public static void close(ResultSet resultSet) throws SQLException {
        if (resultSet != null) {
            resultSet.close();
        }
    }

    public static void close(PreparedStatement preparedStatement) throws SQLException {
        if (preparedStatement != null) {
            preparedStatement.close();
        }
    }
}