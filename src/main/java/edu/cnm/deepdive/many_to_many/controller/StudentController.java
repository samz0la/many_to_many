package edu.cnm.deepdive.many_to_many.controller;

import edu.cnm.deepdive.many_to_many.model.dao.ProjectRepository;
import edu.cnm.deepdive.many_to_many.model.dao.StudentRepository;
import edu.cnm.deepdive.many_to_many.model.entity.Project;
import edu.cnm.deepdive.many_to_many.model.entity.Student;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ExposesResourceFor(Student.class)
@RequestMapping("/students")
public class StudentController {

  private StudentRepository studentRepository;
  private ProjectRepository projectRepository;

  @Autowired
  public StudentController(StudentRepository studentRepository,
      ProjectRepository projectRepository) {
    this.studentRepository = studentRepository;
    this.projectRepository = projectRepository;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Student> list() {
    return studentRepository.findAllByOrderByNameAsc();
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Student> post(@RequestBody Student student) {
    studentRepository.save(student);
    return ResponseEntity.created(student.getHref()).body(student);
  }

  @GetMapping(value = "{studentId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Student get(@PathVariable("studentId") long studentId) {
    return studentRepository.findById(studentId).get();
  }

  @GetMapping(value = "{studentId}", produces = MediaType.TEXT_HTML_VALUE)
  public String getHTML(@PathVariable("studentId") long studentId) {
    return "<html><body>" + studentRepository.findById(studentId).get().getName()
        + "</body></html>";
  }

  // TODONE Add controller method to delete a Student from the database.
  //  Hint: Use the get(long) method to retrieve the student, then delete it using the repository.
  //  (Because of the way the many-to-many relationship is defined in Student vs. Project,
  //  deleting the student automatically cascades to the intermediate table, but not to Project.)
  @DeleteMapping(value = "{studentId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable("studentId") long studentId) {
    studentRepository.deleteById(studentId);
  }

  // TODONE Add controller method to return a list of Project instances for a specified studentId.
  @GetMapping(value = "{studentId}/projects", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Project> projectList(@PathVariable("studentId") long studentID) {
    return get(studentID).getProjects();
  }

  // TODONE Add controller method to add a Project instance to Student with specified studentId.
  //  Hint: retrieve the Student and Project instances from their respective repositories, then
  //  add the Project instance to the student's getProjects() list and save the Student instance.

  @PostMapping(value = "{studentId}/projects", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Project> postProject(@PathVariable("studentId") long studentId,
      @RequestBody Project partialProject) {
    Project project = projectRepository.findById(partialProject.getId()).get();
    Student student = get(studentId);
    student.getProjects().add(project);
    studentRepository.save(student);
    return ResponseEntity.created(project.getHref()).body(project);
  }

  // TODONE Add controller method to remove a Project instance from Student with specified studentId.
  //  Hint: load the Project and Student instances from their respective repositories, then remove
  //  the Project instance from the Student's getProject() list, and save the Student instance.
  @DeleteMapping(value = "{studentId}/projects/{projectId}")
  @ResponseStatus(value = HttpStatus.NO_CONTENT)
  public void deleteProject(@PathVariable("studentId") long studentId,
      @PathVariable("projectId") long projectId) {
    Student student = get(studentId);
    Project project = projectRepository.findById(projectId).get();
    if (student.getProjects().remove(project)) {
      studentRepository.save(student);
    } else {
      throw new NoSuchElementException();
    }
  }


  @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Resource not found")
  @ExceptionHandler(NoSuchElementException.class)
  public void notFound() {
  }

}
