package telsoft.query;

import telsoft.jdbc.OracleConnUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class QueryVer2 {
    public static void main(String[] args) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("src/telsoft/inputdata/conver2.txt"));
            String s = "";
            while ((s = bufferedReader.readLine()) != null) {
                String part[] = s.split(",");
				//Establish a connection and query data
                Connection connection = OracleConnUtils.getOracleConnection();
                PreparedStatement preparedStatement1;
                PreparedStatement preparedStatement2;   // ->use for update

                String strSQLselect = " SELECT ";

                String updateSQL = " UPDATE " +part[0]+ " SET ";
				//-> perform select and update
                for (int i = 1; i< part.length; i++) {
                    strSQLselect+= part[i]+ ", ";
                    if(i == part.length - 1) {
                        updateSQL+= part[i]+ " = ?";
                        break;
                    }
                    updateSQL+= part[i]+ " = ?" +", ";

                }
                strSQLselect+="rowid FROM " +part[0];
                updateSQL+=" WHERE rowid = ?";

                preparedStatement1 = connection.prepareStatement(strSQLselect);
                preparedStatement2 = connection.prepareStatement(updateSQL);

                ResultSet resultSet = preparedStatement1.executeQuery();
                while (resultSet.next()) {
                    List<String> giatri = new ArrayList<String>();
                    String row = resultSet.getString("rowid");
                    preparedStatement2.setNString(part.length, row);
                    for (int i = 0; i < part.length - 1; i++) {
                        giatri.add(convertToComposeUnicode(resultSet.getNString(part[i + 1])));
                        System.out.println(+i  +" :  "+ giatri.get(i));
                        preparedStatement2.setString(i+1, giatri.get(i));
                    }
                    preparedStatement2.executeUpdate();
                }
                preparedStatement1.close();
                preparedStatement2.close();
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String convertToComposeUnicode(String composeUnicode) {
        return Normalizer.normalize(composeUnicode, Normalizer.Form.NFD);
    }
}