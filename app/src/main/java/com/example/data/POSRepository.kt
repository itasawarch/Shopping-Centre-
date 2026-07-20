package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class POSRepository(context: Context) {

    val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "zam_zam_pos_db"
    )
    .fallbackToDestructiveMigration()
    .build()

    val dao = db.posDao()

    // --- Flows ---
    val allProjects: Flow<List<Project>> = dao.getAllProjectsFlow()
    val allServices: Flow<List<Service>> = dao.getAllServicesFlow()
    val allSkills: Flow<List<Skill>> = dao.getAllSkillsFlow()
    val allExperiences: Flow<List<Experience>> = dao.getAllExperiencesFlow()
    val allEducations: Flow<List<Education>> = dao.getAllEducationsFlow()
    val allTestimonials: Flow<List<Testimonial>> = dao.getAllTestimonialsFlow()
    val allBlogs: Flow<List<BlogPost>> = dao.getAllBlogsFlow()
    val allInquiries: Flow<List<Inquiry>> = dao.getAllInquiriesFlow()

    // --- Authentication Support ---
    suspend fun getLoggedInUser(): User? = withContext(Dispatchers.IO) {
        dao.getLoggedInUser()
    }

    suspend fun loginUser(username: String, passwordText: String, rememberMe: Boolean = false): User? = withContext(Dispatchers.IO) {
        val user = dao.getUserByUsername(username)
        if (user != null && user.passwordHash == passwordText) {
            dao.logoutAllUsers()
            val loggedUser = user.copy(isLogged = true, rememberMe = rememberMe)
            dao.updateUser(loggedUser)
            loggedUser
        } else {
            null
        }
    }

    suspend fun logoutUser() = withContext(Dispatchers.IO) {
        dao.logoutAllUsers()
    }

    suspend fun registerUser(username: String, passwordText: String, role: String, displayName: String): Boolean = withContext(Dispatchers.IO) {
        val existing = dao.getUserByUsername(username)
        if (existing != null) {
            false
        } else {
            val newUser = User(
                username = username,
                passwordHash = passwordText,
                role = role,
                displayName = displayName
            )
            dao.insertUser(newUser)
            true
        }
    }

    suspend fun saveAuthSession(session: AuthSession) = withContext(Dispatchers.IO) {
        dao.insertSession(session)
    }

    suspend fun getActiveAuthSession(): AuthSession? = withContext(Dispatchers.IO) {
        dao.getActiveSession()
    }

    suspend fun clearAuthSessions() = withContext(Dispatchers.IO) {
        dao.clearAllSessions()
    }

    // --- CRUD Project Operations ---
    suspend fun saveProject(project: Project) = withContext(Dispatchers.IO) {
        dao.insertProject(project)
    }

    suspend fun deleteProject(project: Project) = withContext(Dispatchers.IO) {
        dao.deleteProject(project)
    }

    // --- CRUD Service Operations ---
    suspend fun saveService(service: Service) = withContext(Dispatchers.IO) {
        dao.insertService(service)
    }

    suspend fun deleteService(service: Service) = withContext(Dispatchers.IO) {
        dao.deleteService(service)
    }

    // --- CRUD Skill Operations ---
    suspend fun saveSkill(skill: Skill) = withContext(Dispatchers.IO) {
        dao.insertSkill(skill)
    }

    suspend fun deleteSkill(skill: Skill) = withContext(Dispatchers.IO) {
        dao.deleteSkill(skill)
    }

    // --- CRUD Experience Operations ---
    suspend fun saveExperience(experience: Experience) = withContext(Dispatchers.IO) {
        dao.insertExperience(experience)
    }

    suspend fun deleteExperience(experience: Experience) = withContext(Dispatchers.IO) {
        dao.deleteExperience(experience)
    }

    // --- CRUD Education Operations ---
    suspend fun saveEducation(education: Education) = withContext(Dispatchers.IO) {
        dao.insertEducation(education)
    }

    suspend fun deleteEducation(education: Education) = withContext(Dispatchers.IO) {
        dao.deleteEducation(education)
    }

    // --- CRUD Testimonial Operations ---
    suspend fun saveTestimonial(testimonial: Testimonial) = withContext(Dispatchers.IO) {
        dao.insertTestimonial(testimonial)
    }

    suspend fun deleteTestimonial(testimonial: Testimonial) = withContext(Dispatchers.IO) {
        dao.deleteTestimonial(testimonial)
    }

    // --- CRUD Blog Operations ---
    suspend fun saveBlog(blog: BlogPost) = withContext(Dispatchers.IO) {
        dao.insertBlog(blog)
    }

    suspend fun deleteBlog(blog: BlogPost) = withContext(Dispatchers.IO) {
        dao.deleteBlog(blog)
    }

    // --- CRUD Contact Inquiry Operations ---
    suspend fun saveInquiry(inquiry: Inquiry) = withContext(Dispatchers.IO) {
        dao.insertInquiry(inquiry)
    }

    suspend fun deleteInquiry(inquiry: Inquiry) = withContext(Dispatchers.IO) {
        dao.deleteInquiry(inquiry)
    }

    // --- Clear All Tables Helper ---
    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        dao.clearAllProjects()
        dao.clearAllServices()
        dao.clearAllSkills()
        dao.clearAllExperiences()
        dao.clearAllEducations()
        dao.clearAllTestimonials()
        dao.clearAllBlogs()
        dao.clearAllInquiries()
    }

    // --- Populate Sample Data / One-Click Demo Import ---
    suspend fun populateSampleDataIfEmpty() = withContext(Dispatchers.IO) {
        // Create Admin user if none exists
        if (dao.getUserByUsername("admin") == null) {
            dao.insertUser(
                User(
                    username = "admin",
                    passwordHash = "admin123",
                    role = "Admin",
                    displayName = "Muhammad Tasawar"
                )
            )
        }

        val projectsList = dao.getAllProjects()
        if (projectsList.isEmpty()) {
            // Seed default services
            val services = listOf(
                Service(title = "WordPress Website Development", description = "Professional, custom-coded or Elementor-based websites tailored to your brand.", iconName = "DeveloperMode"),
                Service(title = "Custom WordPress Design", description = "Hand-crafted pixel-perfect designs with unique Gutenberg blocks and layouts.", iconName = "Palette"),
                Service(title = "E-Commerce Website", description = "Full-featured WooCommerce store integration with secured checkout and payment gateways.", iconName = "ShoppingCart"),
                Service(title = "Business Website", description = "High-performance websites representing corporate brands, service agencies, or startups.", iconName = "Business"),
                Service(title = "Portfolio Website", description = "Interactive CV and showcase sites with beautiful layouts and dark/light toggles.", iconName = "AccountBox"),
                Service(title = "Landing Page Design", description = "Conversion-rate optimized landing pages for marketing, newsletters, or sales funnels.", iconName = "Campaign"),
                Service(title = "Website Speed Optimization", description = "Lightning-fast optimization of Core Web Vitals, images, scripts, and database.", iconName = "Speed"),
                Service(title = "Website Maintenance", description = "Scheduled health checks, updates, bug fixes, and security patches.", iconName = "SettingsSuggest"),
                Service(title = "Bug Fixing", description = "Fast, expert troubleshooting of WordPress, PHP, JavaScript, CSS, or MySQL errors.", iconName = "BugReport"),
                Service(title = "Website Migration", description = "Zero-downtime secure migration of websites to any host or domain.", iconName = "SwapHoriz"),
                Service(title = "SEO Optimization", description = "Technical SEO, meta configurations, schema markup, and robots.txt setup.", iconName = "TrendingUp"),
                Service(title = "Technical Support", description = "24/7 priority support and training for content editors.", iconName = "SupportAgent")
            )
            services.forEach { dao.insertService(it) }

            // Seed default skills
            val skills = listOf(
                Skill(name = "WordPress", progress = 95, category = "CMS / WordPress"),
                Skill(name = "HTML", progress = 90, category = "Core Frontend"),
                Skill(name = "CSS", progress = 90, category = "Core Frontend"),
                Skill(name = "JavaScript", progress = 85, category = "Core Frontend"),
                Skill(name = "PHP", progress = 85, category = "Backend & DB"),
                Skill(name = "MySQL", progress = 80, category = "Backend & DB"),
                Skill(name = "Responsive Design", progress = 95, category = "Core Frontend"),
                Skill(name = "Elementor", progress = 95, category = "CMS / WordPress"),
                Skill(name = "WooCommerce", progress = 90, category = "CMS / WordPress"),
                Skill(name = "SEO", progress = 85, category = "Design & Tools"),
                Skill(name = "GitHub", progress = 80, category = "Design & Tools"),
                Skill(name = "Firebase", progress = 75, category = "Backend & DB"),
                Skill(name = "Flutter (Basic)", progress = 65, category = "Design & Tools"),
                Skill(name = "UI/UX Design", progress = 80, category = "Design & Tools")
            )
            skills.forEach { dao.insertSkill(it) }

            // Seed default experiences
            val experiences = listOf(
                Experience(role = "Senior Web Developer & WP Consultant", company = "Freelance Agency", period = "2022 - Present", description = "Lead development of 120+ custom WordPress platforms and Elementor sites globally. Designed layouts that optimize SEO and mobile loading speed.", type = "Freelance"),
                Experience(role = "Senior Software Engineer", company = "Tech Solutions Corp", period = "2020 - 2022", description = "Architected complex web APIs and optimized database performance, reducing cloud-server load times by 45%. Conducted code reviews and led a team of 4 engineers.", type = "Professional"),
                Experience(role = "Full-Stack Web Developer Intern", company = "Innovate Systems", period = "2019 - 2020", description = "Engineered responsive modern PHP pages and integrated payment gateway channels (Stripe, Paypal) for high-traffic e-commerce hubs.", type = "Internship"),
                Experience(role = "Web Development Mentor", company = "Code Academy", period = "2018 - 2019", description = "Mentored 200+ students in responsive CSS Grid/Flexbox styling, PHP programming, and dynamic theme hooks on WordPress.", type = "Teaching")
            )
            experiences.forEach { dao.insertExperience(it) }

            // Seed default educations
            val educations = listOf(
                Education(degree = "BS Software Engineering", institution = "National University of Sciences", period = "2016 - 2020", description = "Graduated with honors. Focused on Software Architecture, Web Application Architectures, and Relational Databases."),
                Education(degree = "Certified WordPress Developer", institution = "WPMU DEV Academy", period = "2021", description = "Professional credential specializing in advanced hook hooks, theme file directory design, database indexing, and custom multisite setups."),
                Education(degree = "Advanced E-Commerce Architect", institution = "Google Digital Garage", period = "2022", description = "Expert certification in digital marketing, Google Analytics event tracking, conversion-rate optimization (CRO), and Local SEO configurations."),
                Education(degree = "Responsive Web Design Specialist", institution = "freeCodeCamp", period = "2020", description = "Comprehensive credential covering accessible typography, WCAG contrast standards, CSS Media queries, and grid-breaking visual asymmetry.")
            )
            educations.forEach { dao.insertEducation(it) }

            // Seed default projects
            val projects = listOf(
                Project(
                    title = "EcoShop Storefront",
                    description = "Premium green-tech e-commerce catalog featuring highly responsive grid listings, custom attribute filtering, and speed-optimized WooCommerce flows.",
                    technologies = "WordPress, WooCommerce, Elementor, Stripe, PHP",
                    imageUrl = "https://images.unsplash.com/photo-1472851294608-062f824d29cc?auto=format&fit=crop&q=80&w=400",
                    demoUrl = "https://eco-shop-demo.example.com",
                    githubUrl = "https://github.com/tasawar/ecoshop-woo",
                    isFeatured = true,
                    category = "WordPress"
                ),
                Project(
                    title = "MedHub Medical Portal",
                    description = "Fast-loading online patient appointment booking and scheduling clinical dashboard styled with clean, custom meta-fields and accessible layouts.",
                    technologies = "WordPress, Custom Post Types, PHP, MySQL, CSS",
                    imageUrl = "https://images.unsplash.com/photo-1505751172876-fa1923c5c528?auto=format&fit=crop&q=80&w=400",
                    demoUrl = "https://medhub-portal.example.com",
                    githubUrl = "https://github.com/tasawar/medhub-portal",
                    isFeatured = true,
                    category = "Web"
                ),
                Project(
                    title = "Modern Agency Showcase",
                    description = "Conversion-optimized landing page featuring animated key counters, sticky headers, a collapsible newsletter subscription bar, and 100/100 Core Web Vitals.",
                    technologies = "WordPress, Gutenberg Blocks, Elementor, CSS Optimization",
                    imageUrl = "https://images.unsplash.com/photo-1460925895917-afdab827c52f?auto=format&fit=crop&q=80&w=400",
                    demoUrl = "https://agency-speed-demo.example.com",
                    githubUrl = "https://github.com/tasawar/modern-agency-wp",
                    isFeatured = false,
                    category = "WordPress"
                ),
                Project(
                    title = "Personal Portfolio App",
                    description = "Elegant Jetpack Compose Showcase application featuring native dark mode toggles, localized SQLite Room caching, and animated visual timeline components.",
                    technologies = "Kotlin, Jetpack Compose, Room DB, Coil",
                    imageUrl = "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&q=80&w=400",
                    demoUrl = "https://ais-dev-t5nwckh7aktofzbkfmlx36-264201700240.asia-southeast1.run.app",
                    githubUrl = "https://github.com/tasawar/compose-developer-portfolio",
                    isFeatured = true,
                    category = "Mobile"
                )
            )
            projects.forEach { dao.insertProject(it) }

            // Seed testimonials
            val testimonials = listOf(
                Testimonial(clientName = "Sarah Jenkins", company = "CEO @ GreenCo", rating = 5, feedback = "Tasawar is an incredible software engineer. He built our EcoShop storefront with incredible visual polish. Page-load speeds decreased significantly, and WooCommerce checkouts are smooth on mobile devices!"),
                Testimonial(clientName = "Abdul Rehman", company = "Founder @ MedHub", rating = 5, feedback = "Exceptional WordPress designer and PHP troubleshooter. He successfully indexed a custom post query that was lagging, and completed our medical system migration with absolutely zero downtime."),
                Testimonial(clientName = "Sophia Liang", company = "Product Director @ DesignLab", rating = 5, feedback = "A true professional. He converted our Figma designs into responsive, accessible Gutenberg blocks. Highly communicative and deliver fast!")
            )
            testimonials.forEach { dao.insertTestimonial(it) }

            // Seed default blogs
            val blogs = listOf(
                BlogPost(
                    title = "Website Speed Optimization: The Ultimate Guide",
                    content = "Site performance directly impacts your conversions and Google SEO rankings. In this technical deep-dive, we explore how configuring image lazy-loading, serving next-gen webp images, minifying critical CSS paths, and utilizing Redis object-caching can bring your page loading speeds down to less than 1.5 seconds. Learn the hooks and setup rules that elevate your Google PageSpeed scores to a perfect 100/100.",
                    category = "Speed & Performance",
                    date = "July 15, 2026"
                ),
                BlogPost(
                    title = "Demystifying Custom Gutenberg Blocks",
                    content = "While visual page builders like Elementor are great for rapid layout generation, custom-coded block themes built with Gutenberg offer unmatched, bloat-free load speeds. In this tutorial, we will write a responsive custom hero slider using PHP rendering and Tailwind CSS. Learn how to design a pixel-perfect, Gutenberg-friendly experience with custom meta keys.",
                    category = "WordPress Development",
                    date = "July 10, 2026"
                ),
                BlogPost(
                    title = "Technical SEO Best Practices for WooCommerce",
                    content = "Succeeding in dynamic e-commerce requires search crawlers to fully index your products. Explore why adding robust Product Schema markup, configuring custom XML sitemaps, optimizing image alt tag descriptors, and fine-tuning robots.txt parameters can drive higher organic search impressions. Learn about configuring breadcrumb structured-data in a couple of minutes.",
                    category = "SEO & Marketing",
                    date = "July 02, 2026"
                )
            )
            blogs.forEach { dao.insertBlog(it) }

            // Seed inquiries
            val inquiries = listOf(
                Inquiry(name = "John Doe", email = "john.doe@example.com", phone = "0321-4433221", message = "Hi Tasawar, I would love to hire you to build a custom WordPress business directory. It requires advanced filtering and WooCommerce support. Let's schedule a call!", status = "Unread"),
                Inquiry(name = "Kamil Shah", email = "kamil.shah@innovate.pk", phone = "0312-5566778", message = "Hello, your speed optimization services are exactly what our startup needs. Our store takes 5+ seconds to load. Do you have availability next week?", status = "Unread")
            )
            inquiries.forEach { dao.insertInquiry(it) }
        }
    }
}
