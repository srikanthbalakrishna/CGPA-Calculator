import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javafx.application.*;
import javafx.event.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.*;
public class CGPA_Calculator extends Application 
{
    Stage window;
    
    static int max_num_of_courses;
    public static void main(String args[]) throws Exception
    {   
        max_num_of_courses = 9;
        
        launch(args); //Launch GUI    
        
        System.out.println("Back to main");
    }      
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;
        window.setTitle("CGPA Calculator - JavaFX - JDBC");

        //GridPane with 10px padding around edge
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);

        //Course Label - constrains use (child, column, row)
        Label nameLabel = new Label("#");
        GridPane.setConstraints(nameLabel, 0, 0);

        //Grade Label
        Label gradeLabel = new Label("GPA:");
        GridPane.setConstraints(gradeLabel, 1, 0);
        
        //Credit Label
        Label credLabel = new Label("Semester Credits:");
        GridPane.setConstraints(credLabel, 2, 0);
        
        grid.getChildren().addAll(nameLabel, gradeLabel, credLabel);//adding labels
 
        Label sem_no ;
        
        TextField gradeInputs[] = new TextField[max_num_of_courses];
        TextField credInputs[] = new TextField[max_num_of_courses];
        for(int i=0 ; i<max_num_of_courses-1 ; i++)//adding input text fields
        {
            
            
            gradeInputs[i] = new TextField();
            GridPane.setConstraints(gradeInputs[i],1,i+1);
            
            credInputs[i] = new TextField();
            GridPane.setConstraints(credInputs[i],2,i+1);
            
            sem_no = new Label(Integer.toString(i+1));
            GridPane.setConstraints(sem_no,0,i+1);
            grid.getChildren().addAll(sem_no,gradeInputs[i],credInputs[i]);
        }


        //Calculate button
        Button calcButton = new Button("Calculate");
        GridPane.setConstraints(calcButton, 1,max_num_of_courses+1 );

    
        grid.getChildren().add(calcButton); //adding calculate button

        Scene scene = new Scene(grid, 400, 400);
        window.setScene(scene);
        window.show();
        
        calcButton.setOnAction( (e)->store_into_database(gradeInputs,credInputs) );
        //Once calcButton is clicked, it will calculate the grades and display
        //a popup with final CGPA
    }
    
    public static void store_into_database(TextField[] gradeInputs,TextField[] credInputs) 
    {
        String URL = "jdbc:mysql://localhost:3306/demo_jdbc";
        String uname = "root";
        String pass = "";
        int rows_affected;
        try
        {
        String query = "insert into grades values (?,?)"; //query to store value
        Class.forName("com.mysql.jdbc.Driver");
        Connection con = DriverManager.getConnection(URL,uname,pass);
        con.createStatement().executeUpdate("truncate table grades"); // Clear table contents
        PreparedStatement st = con.prepareStatement(query);
        
        for(int i=0;i<max_num_of_courses-1;i++)
        {
            //skip the fields left empty
            if(gradeInputs[i].getText().trim().equals("") || credInputs[i].getText().trim().equals("") )
                continue;
            //storing grade(GPA) and credits as strings
            st.setString(1, gradeInputs[i].getText());
            st.setString(2, credInputs[i].getText());
            rows_affected = st.executeUpdate();
        }
        //Finished storing
        System.out.println("Finished Storing");
        
        
        
        AlertBox.display("Final CGPA", calculate());
        //now to display the grade
        
        }catch(Exception e)
        {
            System.out.println(e);
        }
        
    }
    
    public static String calculate() throws Exception
    {
        //retrieves data from DB
        String URL = "jdbc:mysql://localhost:3306/demo_jdbc";
        String uname = "root";
        String pass = "";
        String query = "insert into grades values (?,?)"; //query to store value
        Class.forName("com.mysql.jdbc.Driver");
        Connection con = DriverManager.getConnection(URL,uname,pass);
        Statement stat = con.createStatement();
        ResultSet rs = stat.executeQuery("select * from grades");
        double grades[] = new double[max_num_of_courses];
        int creds[] = new int[max_num_of_courses];
        
        for(int i=0;rs.next();i++)
        {
            grades[i] = rs.getDouble("grade");
            creds[i] = rs.getInt("credits");
        }//fetched DB data
        
        int total_credits = 0;
        double sum_cred_x_grade = 0;
        //
        for(int i=0;i<max_num_of_courses-1;i++)
        {
           
            if(creds[i]==0 || grades[i]==0.0)
                continue;
            
            total_credits += creds[i];
            sum_cred_x_grade += grades[i]*creds[i];
        }
        //
        double cgpa = sum_cred_x_grade/(double)total_credits;
        String formato = String.format("%.2f",cgpa);
        String s = "CGPA = "+formato+"\nTotal Credits = " + total_credits;
        return s;
    }
    
}
