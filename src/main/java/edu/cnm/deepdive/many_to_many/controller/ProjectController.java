package edu.cnm.deepdive.many_to_many.controller;

import edu.cnm.deepdive.many_to_many.model.dao.ProjectRepository;
import edu.cnm.deepdive.many_to_many.model.dao.StudentRepository;
import edu.cnm.deepdive.many_to_many.model.entity.Project;
import edu.cnm.deepdive.many_to_many.model.entity.Student;
import java.util.List;
import java.util.NoSuchElementException;
import javax.transaction.Transactional;
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
@ExposesResourceFor(Project.class)
@RequestMapping("/projects")
public class ProjectController {

  private ProjectRepository projectRepository;
  private StudentRepository studentRepository;

  @Autowired
  public ProjectController(
      ProjectRepository projectRepository,
      StudentRepository studentRepository) {
    this.projectRepository = projectRepository;
    this.studentRepository = studentRepository;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Project> list() {
    return projectRepository.findAllByOrderByNameAsc();
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Project> post(@RequestBody Project project) {
    projectRepository.save(project);
    return ResponseEntity.created(project.getHref()).body(project);
  }

  @GetMapping(value = "{projectId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Project get(@PathVariable("projectid") long projectId){
    return projectRepository.findById(projectId).get();
  }

  @Transactional
  @DeleteMapping(value = "{projectId}")
  @ResponseStatus(value = HttpStatus.NO_CONTENT)
  public void delete(@PathVariable("projectId") long projectId){
    Project project = get(projectId);
    List<Student> students =project.getStudents();
    for (Student student : students){
      student.getProjects().remove(project);
    }
    studentRepository.saveAll(students);
    projectRepository.delete(project);
  }

  // TODONE Add controller method to return list of Student instances for a specified projectId.
@GetMapping("{projectId}/students")
public List<Student>studentList(@PathVariable("projectId")long projectId){
    return get(projectId).getStudents();
}

  @PostMapping(value = "{projectId}/students", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Project> postStudent(@PathVariable("projectId") long projectId,
      @RequestBody Student partialStudent){
    Project project = get(projectId);
    Student student = studentRepository.findById(partialStudent.getId()).get();
    student.getProjects().add(project);
    studentRepository.save(student);
    return ResponseEntity.created(project.getHref()).body(project);
  }

  // TODONE Add controller method to remove a Student instance from project with specified projectId.
  //  Hint: Remove the project from the student's getProjects() list, and save the student.
  @DeleteMapping(value = "{projectId}/students/{studentId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteStudent(@PathVariable("projectId") long projectId,
      @PathVariable("studentId") long studentId){
    Project project = get(projectId);
    Student student = studentRepository.findById(studentId).get();
    if (project.getStudents().remove(student)){
      projectRepository.save(project);
    }else{
      throw new NoSuchElementException();
    }
  }

  @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Resource not found")
  @ExceptionHandler(NoSuchElementException.class)
  public void notFound(){}
}
