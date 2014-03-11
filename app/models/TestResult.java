package models;

import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;

@Entity 
public class TestResult extends Model {

  @Id
  public Long id;
  
  @Constraints.Required
  public Long value;
  
  @Constraints.Required
  public String testName;
  
  @Constraints.Required
  public String backEnd;
  
  
  @Formats.DateTime(pattern="dd/MM/yyyy")
  public Date addDate = new Date();
  
  public static Finder<Long,Task> find = new Finder<Long,Task>(
    Long.class, Task.class
  ); 

}
