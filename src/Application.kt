package com.example

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*

val students = mutableListOf<Student>().apply {
    add(Student("1","Mahmoud" , 15))
}

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    // install ContentNegotiation to handle headers also add json for serialization
    install(ContentNegotiation) {
        json ()
    }

    routing {
        get("/"){
            call.respondText("HELLO WORLD")
        }
        // build route for student
        route("/student"){

            // get request to this route
            get {
                // check if there is students or not
                if (students.isNotEmpty()){
                    call.respond(students)
                }else{
                    call.respondText("Sorry no students added yet", status = HttpStatusCode.NotFound)
                }
            }

            // accept id of studnet
            get("{id}"){
                // check if request has `id` parameter or not
                val id = call.parameters["id"] ?: return@get call.respondText("Missing Paramater [id]", status = HttpStatusCode.BadRequest)
                // check exist of student
                val student = students.find { it.id == id } ?: return@get call.respondText("No studend with id $id",status = HttpStatusCode.NotFound )
                // return that student
                call.respond(student)

            }

            // add a student
            post {
                try {
                    // `receive` will get content from request and convert it to Student object
                    val student = call.receive<Student>()
                    if (students.filter { it.id == student.id }.isEmpty()) {
                        students.add(student)
                        call.respondText("Added student ${student.name}", status = HttpStatusCode.Created)
                    } else {
                        call.respondText("Already exist student with same id", status = HttpStatusCode.BadRequest)
                    }
                } catch (e: Exception) {
                    call.respondText("Error ${e.message}", status = HttpStatusCode.BadRequest)
                }
            }

            put("{id}") {
                try {
                    val id = call.parameters["id"] ?: return@put call.respondText(
                        "Missing Paramater [id]",
                        status = HttpStatusCode.BadRequest
                    )
                    // `receive` will get content from request and convert it to Student object
                    val student = call.receive<Student>()
                    if (id != student.id) {
                        call.respondText("Not matched student id $id", status = HttpStatusCode.BadRequest)
                        return@put
                    }
                    val index = students.indexOfFirst { it.id == id }
                    if (index != -1) {
                        students[index] = student
                        call.respondText("updated student $student", status = HttpStatusCode.OK)
                    } else {
                        call.respondText("not exist student with id ", status = HttpStatusCode.BadRequest)
                    }
                } catch (e: Exception) {
                    call.respondText("Error ${e.message}", status = HttpStatusCode.BadRequest)
                }
            }

            // delete a student with `id`
            delete("{id}") {
                val id = call.parameters["id"] ?: return@delete call.respondText(
                    "Missing Paramater [id]",
                    status = HttpStatusCode.BadRequest
                )
                val student = students.find { it.id == id } ?: return@delete call.respondText(
                    "No students with id $id",
                    status = HttpStatusCode.NotFound
                )
                students.remove(student)
                call.respondText("Deleted", status = HttpStatusCode.Accepted)
            }


        }
    }
}

