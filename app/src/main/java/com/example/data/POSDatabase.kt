package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

// ==========================================
// 1. PORTFOLIO ENTITIES
// ==========================================

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val username: String,
    val passwordHash: String,
    val role: String, // "Admin", "Manager", "Viewer"
    val displayName: String,
    val rememberMe: Boolean = false,
    val isLogged: Boolean = false
)

@Entity(tableName = "auth_sessions")
data class AuthSession(
    @PrimaryKey val userId: String,
    val token: String,
    val role: String,
    val username: String,
    val displayName: String,
    val permissions: String,
    val loginTimestamp: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L),
    val isActive: Boolean = true
)

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val technologies: String, // Comma separated list (e.g. "WordPress, PHP, MySQL, CSS")
    val imageUrl: String,
    val demoUrl: String,
    val githubUrl: String,
    val isFeatured: Boolean = false,
    val category: String = "Web" // "Web", "WordPress", "Mobile", "UI/UX"
)

@Entity(tableName = "services")
data class Service(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val iconName: String // Icon name token mapped to Material Icons
)

@Entity(tableName = "skills")
data class Skill(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val progress: Int, // 0 to 100
    val category: String // "Core Frontend", "Backend & DB", "CMS / WordPress", "Design & Tools"
)

@Entity(tableName = "experiences")
data class Experience(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val role: String,
    val company: String,
    val period: String, // "2024 - Present"
    val description: String,
    val type: String // "Professional", "Freelance", "Internship", "Teaching"
)

@Entity(tableName = "educations")
data class Education(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val degree: String,
    val institution: String,
    val period: String,
    val description: String
)

@Entity(tableName = "testimonials")
data class Testimonial(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val clientName: String,
    val feedback: String,
    val rating: Int, // 1 to 5
    val company: String
)

@Entity(tableName = "blogs")
data class BlogPost(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val category: String,
    val date: String,
    val author: String = "Muhammad Tasawar",
    val imageUrl: String = ""
)

@Entity(tableName = "inquiries")
data class Inquiry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val phone: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Unread" // "Unread", "Replied", "Archived"
)

// ==========================================
// 2. DATA ACCESS OBJECT (DAO)
// ==========================================

@Dao
interface POSDao {

    // --- Users ---
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE isLogged = 1 LIMIT 1")
    suspend fun getLoggedInUser(): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE users SET isLogged = 0")
    suspend fun logoutAllUsers()

    // --- Auth Sessions ---
    @Query("SELECT * FROM auth_sessions WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveSession(): AuthSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AuthSession)

    @Query("UPDATE auth_sessions SET isActive = 0")
    suspend fun deactivateAllSessions()

    @Query("DELETE FROM auth_sessions")
    suspend fun clearAllSessions()

    // --- Projects ---
    @Query("SELECT * FROM projects ORDER BY title ASC")
    fun getAllProjectsFlow(): Flow<List<Project>>

    @Query("SELECT * FROM projects")
    suspend fun getAllProjects(): List<Project>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)

    @Query("DELETE FROM projects")
    suspend fun clearAllProjects()

    // --- Services ---
    @Query("SELECT * FROM services ORDER BY title ASC")
    fun getAllServicesFlow(): Flow<List<Service>>

    @Query("SELECT * FROM services")
    suspend fun getAllServices(): List<Service>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: Service)

    @Delete
    suspend fun deleteService(service: Service)

    @Query("DELETE FROM services")
    suspend fun clearAllServices()

    // --- Skills ---
    @Query("SELECT * FROM skills ORDER BY progress DESC")
    fun getAllSkillsFlow(): Flow<List<Skill>>

    @Query("SELECT * FROM skills")
    suspend fun getAllSkills(): List<Skill>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: Skill)

    @Delete
    suspend fun deleteSkill(skill: Skill)

    @Query("DELETE FROM skills")
    suspend fun clearAllSkills()

    // --- Experiences ---
    @Query("SELECT * FROM experiences ORDER BY period DESC")
    fun getAllExperiencesFlow(): Flow<List<Experience>>

    @Query("SELECT * FROM experiences")
    suspend fun getAllExperiences(): List<Experience>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperience(experience: Experience)

    @Delete
    suspend fun deleteExperience(experience: Experience)

    @Query("DELETE FROM experiences")
    suspend fun clearAllExperiences()

    // --- Educations ---
    @Query("SELECT * FROM educations ORDER BY period DESC")
    fun getAllEducationsFlow(): Flow<List<Education>>

    @Query("SELECT * FROM educations")
    suspend fun getAllEducations(): List<Education>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEducation(education: Education)

    @Delete
    suspend fun deleteEducation(education: Education)

    @Query("DELETE FROM educations")
    suspend fun clearAllEducations()

    // --- Testimonials ---
    @Query("SELECT * FROM testimonials")
    fun getAllTestimonialsFlow(): Flow<List<Testimonial>>

    @Query("SELECT * FROM testimonials")
    suspend fun getAllTestimonials(): List<Testimonial>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestimonial(testimonial: Testimonial)

    @Delete
    suspend fun deleteTestimonial(testimonial: Testimonial)

    @Query("DELETE FROM testimonials")
    suspend fun clearAllTestimonials()

    // --- Blogs ---
    @Query("SELECT * FROM blogs ORDER BY date DESC")
    fun getAllBlogsFlow(): Flow<List<BlogPost>>

    @Query("SELECT * FROM blogs ORDER BY date DESC")
    suspend fun getAllBlogs(): List<BlogPost>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlog(blog: BlogPost)

    @Delete
    suspend fun deleteBlog(blog: BlogPost)

    @Query("DELETE FROM blogs")
    suspend fun clearAllBlogs()

    // --- Inquiries ---
    @Query("SELECT * FROM inquiries ORDER BY timestamp DESC")
    fun getAllInquiriesFlow(): Flow<List<Inquiry>>

    @Query("SELECT * FROM inquiries ORDER BY timestamp DESC")
    suspend fun getAllInquiries(): List<Inquiry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInquiry(inquiry: Inquiry)

    @Delete
    suspend fun deleteInquiry(inquiry: Inquiry)

    @Query("DELETE FROM inquiries")
    suspend fun clearAllInquiries()
}

// ==========================================
// 3. DATABASE CLASS
// ==========================================

@Database(
    entities = [
        User::class,
        AuthSession::class,
        Project::class,
        Service::class,
        Skill::class,
        Experience::class,
        Education::class,
        Testimonial::class,
        BlogPost::class,
        Inquiry::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun posDao(): POSDao
}
