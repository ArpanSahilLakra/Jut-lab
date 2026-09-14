import re

with open('app/src/main/java/com/example/data/LabDatabase.kt', 'r') as f:
    content = f.read()

# Generate the block to insert units and topics
new_data = """
        // --- UNITS & TOPICS FOR BSP01 (Physics) ---
        val physUnit1 = UnitEntity(
            id = "u_bsp01_1", subjectId = "sub_bsp01", unitNumber = 1,
            title = "Measurements & Units", description = "SI units, errors, accuracy.",
            version = "NEP-2023", source = "JUT"
        )
        val physUnit2 = UnitEntity(
            id = "u_bsp01_2", subjectId = "sub_bsp01", unitNumber = 2,
            title = "Semiconductor Physics", description = "Conductors, semiconductors, insulators.",
            version = "NEP-2023", source = "JUT"
        )
        academicDao.insertUnits(listOf(physUnit1, physUnit2))
        
        val physTopic1 = TopicEntity(
            id = "t_bsp01_1_1", unitId = "u_bsp01_1",
            title = "SI System & Errors", description = "Dimensional analysis, percentage error.",
            contentEnglish = "The SI system is the standard. Errors can be absolute or relative.",
            version = "NEP-2023", source = "JUT"
        )
        val physTopic2 = TopicEntity(
            id = "t_bsp01_2_1", unitId = "u_bsp01_2",
            title = "Energy Bands", description = "Intrinsic and extrinsic semiconductors.",
            contentEnglish = "Semiconductors have a narrow band gap.",
            version = "NEP-2023", source = "JUT"
        )
        academicDao.insertTopics(listOf(physTopic1, physTopic2))

        // --- UNITS & TOPICS FOR BSM01 (Math I) ---
        val mathUnit1 = UnitEntity(
            id = "u_bsm01_1", subjectId = "sub_bsm01", unitNumber = 1,
            title = "Algebra & Matrices", description = "Determinants, Matrix algebra.",
            version = "NEP-2023", source = "JUT"
        )
        academicDao.insertUnits(listOf(mathUnit1))
        
        val mathTopic1 = TopicEntity(
            id = "t_bsm01_1_1", unitId = "u_bsm01_1",
            title = "Matrix Inverse", description = "Finding the inverse of a 3x3 matrix.",
            contentEnglish = "A matrix inverse A^-1 exists if det(A) != 0.",
            version = "NEP-2023", source = "JUT"
        )
        academicDao.insertTopics(listOf(mathTopic1))
        
        // --- ADDING BSM01 QUESTIONS ---
        val mq1 = QuestionEntity(
            id = "q_sem1_math_1",
            semesterId = "sem_1",
            subjectId = "sub_bsm01",
            unitId = "u_bsm01_1",
            topicId = "t_bsm01_1_1",
            questionTextEnglish = "When does a matrix NOT have an inverse?",
            questionType = "CONCEPTUAL",
            difficulty = "Basic",
            priority = "Important",
            sourceType = "PRACTICE",
            fullAnswerEnglish = "A matrix does not have an inverse if its determinant is zero. Such a matrix is called a singular matrix."
        )
        val mq2 = QuestionEntity(
            id = "q_sem1_math_2",
            semesterId = "sem_1",
            subjectId = "sub_bsm01",
            unitId = "u_bsm01_1",
            topicId = "t_bsm01_1_1",
            questionTextEnglish = "What is the rank of a matrix?",
            questionType = "DEFINITION",
            difficulty = "Intermediate",
            priority = "VVI",
            sourceType = "PRACTICE",
            fullAnswerEnglish = "The rank of a matrix is the maximum number of linearly independent row vectors (or column vectors) in the matrix."
        )
        academicDao.insertQuestions(listOf(mq1, mq2))
"""

# Now we need to update the existing inserted questions in Physics/EE to include their unitId and topicId if possible.
# Let's just find the `academicDao.insertQuestions(listOf(q1, q2, q3, q4, q5, q6, q7, q8, q9, q10))`
# and insert this new data right after it.

content = re.sub(
    r'(academicDao\.insertQuestions\(listOf\(q1, q2, q3, q4, q5, q6, q7, q8, q9, q10\)\))',
    r'\1\n' + new_data,
    content
)

# Modify q1..q10 definitions to include unitId and topicId where applicable
content = content.replace(
    'subjectId = "sub_bsp01",\n            questionTextEnglish = "What is the SI system',
    'subjectId = "sub_bsp01",\n            unitId = "u_bsp01_1",\n            topicId = "t_bsp01_1_1",\n            questionTextEnglish = "What is the SI system'
)
content = content.replace(
    'subjectId = "sub_bsp01",\n            questionTextEnglish = "What is a semiconductor?',
    'subjectId = "sub_bsp01",\n            unitId = "u_bsp01_2",\n            topicId = "t_bsp01_2_1",\n            questionTextEnglish = "What is a semiconductor?'
)

with open('app/src/main/java/com/example/data/LabDatabase.kt', 'w') as f:
    f.write(content)
