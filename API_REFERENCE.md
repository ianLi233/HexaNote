# HexaNote Backend API Reference

## Base URL
```
http://localhost:8001/api/v1
```

## Authentication

### Getting Your Password

The default password is **`hexanote`**. You can customize this by setting the `SIMPLE_PASSWORD` environment variable in your `.env` file or `docker-compose.yml`.

**Location in code:**
- Default value: [backend/config.py:40](backend/config.py#L40)
- Environment variable: `SIMPLE_PASSWORD` in [backend/.env.example:32](backend/.env.example#L32)

### Get Access Token

Authenticate and receive a JWT token valid for 7 days (10080 minutes).

**Endpoint:** `POST /api/v1/token`

**Request:**
```json
{
  "password": "hexanote"
}
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiZXhwIjoxNzcxMDE5OTI4fQ.xCoCqzsmtk_sF5tJWaz9rLbcSJz2MFg_QiTlss6-gno",
  "token_type": "bearer",
  "expires_in": 604800
}
```

**Windows Client Example:**
```typescript
// In windows-client/src/services/api.ts
const api = axios.create({
    baseURL: 'http://localhost:8001/api/v1',
    headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer YOUR_TOKEN_HERE'
    },
})
```

### Register Device

Register a new device for sync functionality.

**Endpoint:** `POST /api/v1/devices/register`

**Request:**
```json
{
  "device_name": "My Windows PC",
  "device_type": "windows"
}
```

**Response:**
```json
{
  "device_id": "a0c28ba6-dab2-4752-8d90-267535627f2d",
  "message": "Device registered successfully"
}
```

### Health Check

Check the status of the API and its dependent services.

**Endpoint:** `GET /api/v1/health`

**Response:**
```json
{
  "status": "healthy",
  "services": {
    "database": true,
    "weaviate": true
  },
  "version": "1.0.0"
}
```

---

## Notes API

### List Notes

Get all notes with pagination and optional tag filtering.

**Endpoint:** `GET /api/v1/notes`

**Query Parameters:**
- `page` (integer, default: 1) - Page number
- `limit` (integer, default: 50, max: 100) - Items per page
- `tags` (string, optional) - Comma-separated tags to filter by

**Windows Client Example:**
```typescript
// windows-client/src/services/api.ts:54-58
const response = await api.get<{ notes: Note[] }>('/notes')
return response.data.notes
```

**Response:**
```json
{
  "notes": [
    {
      "title": "Uploaded notes",
      "content": "text to upload\n\nlocal text local text local text local text local text\nlocal text local text local text local text local text\nlocal text local text local text local text local text\nlocal text local text local text local text local text\nlocal text local text local text local text local text\nlocal text local text local text local text local text\n",
      "tags": [],
      "id": "443ac06d-8b74-481d-bd7a-2523d4ae9da6",
      "version": 1,
      "weaviate_uuid": "b3c0b8fb-21ed-4ba7-8d4c-5908da981700",
      "created_at": "2026-02-06T23:18:04",
      "updated_at": "2026-02-06T23:18:10",
      "deleted_at": null
    },
    {
      "title": "hello",
      "content": "# hello\n\nhello hello hello\n",
      "tags": [],
      "id": "b8700f8a-91e8-445b-8907-a33933aec412",
      "version": 1,
      "weaviate_uuid": "1c7d7ed0-9575-4c3a-ae46-22a0ed908442",
      "created_at": "2026-02-06T19:12:23",
      "updated_at": "2026-02-06T23:06:34",
      "deleted_at": null
    },
    {
      "title": "E4040",
      "content": "# E4040\n\n\nZoran Kostic Columbia Site\t\nHomeCoursesPeopleNewsResearch & TA/CA OpportunitiesPostdocPublications\t\nNeural Networks and Deep Learning\nNeural Networks and Deep Learning\nColumbia University course ECBM E4040\nZoran Kostic, Ph.D., Dipl. Ing.,  Professor of Professional Practice, zk2172(at)columbia.edu\nElectrical Engineering Department, Data Sciences Institute,  Columbia University in the City of New York\nCourse in a nutshell: \n Theoretical underpinnings and practical aspects of Neural Networks and Deep Learning. Convolutional and Recurrent Neural Networks. Focus on applications and projects.\nBulletin Description:\nDeveloping features & internal representations of the world, artificial neural networks, classifying handwritten digits with logistics regression, feedforward deep networks, back propagation in multilayer perceptrons, regularization of deep or distributed models, optimization for training deep models, convolutional neural networks, recurrent and recursive neural networks, deep learning in speech and object recognition.\n\nRegistration:\nTo register into the course: (i) students need to get onto the Columbia SSOL waitlist, and (ii) populate this form (using CU UNI email). Those students who satisfy the requirements will be moved from the waitlist into the registered list by early September. The course can be taken by undergraduate and graduate student. \nPrerequisites:\nRequired prerequisites: knowledge of linear algebra, probability and statistics, programming. Machine learning must be taken as an academic course either before or in parallel to this course.\n\nOrganization\nLectures:\n\nPresentation of material by instructors and guest lecturers\n\nAssignments:\n\nCombination of analytical and programming assignments\n\nExam and quizzes.\n\nProjects:\n\nTeam-based\n\nStudents with complementary backgrounds\n\nEngineering report\n\nContent\nIntroduction to neural networks.\n\nConvolutional and recurrent neural networks.\n\nFocuses on the intuitive understanding of deep learning.\n\nReview of underpinning theory - linear algebra, statistics, machine learning.\n\nAnalytical study and software design.\n\nThree-four assignments in Python and one DL framework (Tensorflow or PyTorch)\n\nSignificant project.\n\nEnables further exploration of key concepts in deep learning.\n\nTypical Syllabus\nIntroduction to Course E4040\n\nIntroduction to Deep Learning (DL)\n\nIntroduction to DL computing Resources\n\nMachine Learning Algorithms\n\nt-SNE Data Visualization\n\nUniversal approximation theorem - Visual proof\n\nAlgebra review \n\nDeep Feed-forward Networks\n\nBack Propagation\n\nOptimization\n\nConvolutional Neural Nets (CNNs)\n\nCNN applications\n\nCNN Examples\n\nRegularization\n\nPractical Methodology for Deep Learning\n\nRecurrent Neural Nets (RNNs)\n\nRNN applications\n\nDeel Learning Applications\n\nAutoencoders\n\nGenerative Networks GANs, Variational Encoders\n\nGenerative Models\n\nGuest Lectures\n\nDeep Learning Trends\n\nBooks, Tools and Resources\nBOOKS:\n\nDeep Learning, Ian Goodfellow, Yoshua Bengio, and Aaron Courville, The MIT Press, 2016.\n\n2017-2026 software platform:\n\nGoogle TensorFlow, Google Cloud (GCP), Python, Github, Google Colab\n\n2016 software platform:\n\nTheano\n\nNvidia’s Deep Neural Network Library (cuDNN)\n\nAmazon AWS cloud tools, Code development on github, bitbucket\n\nProject Areas\nMedical\n\nAutonomous cars\n\nEnvironmental\n\nSmart cities\n\nPhysical data analytics\n\n2024 Fall Projects\nConvMixer: Patches are all you need?\nAntibody-Antigen Interaction Prediction with Modified CNN Model and Encoding Comparisons\nAttention-Guided Version of 2D UNet for Automatic Brain Tumor Segmentation\nConvMixer: Patches are all you need?\nDeep Clustering for Unsupervised Learning of Visual Features\nDensely Connected Convolutional Networks\nGlobal Filter Networks for Image Classiﬁcation\nImproving CNN Robustness via CS Shapley Value-guided Augmentation\nKAN: Kolmogorov–Arnold Networks \nLSTM Fully Convolutional Networks for Time Series Classification\nResidual Attention Network for Image Classification\nShow, Attend and Tell: Neural Image Caption Generation with Visual Attention\nSimCLR - A Simple Framework for Contrastive Learning of Visual Representations\nSwin Transformer: Hierarchical Vision Transformer Using Shifted Windows\nUsing DUCK-Net for Polyp Image Segmentation\nWalsh-Hadamard Transforms in Neural Networks\n2022 Fall Projects\nBinaryConnect: Training Deep Neural Networks with binary weights during propagations\nApplication of transformer model for the time series forecast\nDeep Learning for Symbolic Mathematics\nImage Demoireing with Learnable Bandpass Filters\nIoU comparison: DIoU, CIoU\nJoint Face Detection and Alightment using Multi-task Cascaded CNN\nMobileNets: Efficient Convolutional Neural Networks for Mobile Vision Applications\nOptical Coherence Tomography Enabled Classification of the Pulmonary Vein\nRegistration of Optical Coherence Tomography Volumes Subject to Rotation, Translation and Occlusion\nSinging Voice Separation from Monaural Recordings Using Deep Recurrent Neural Networks\nSingle-Image Depth Perception in the Wild\nSpectral Representations for Convolutional Neural Networks\nSwin Transformer: Hierarchical Vision Transformer Using Shifted Windows\nSwin Transformer: Hierarchical Vision Transformer Using Shifted Windows\nUncertainty Estimation Using a Single Deep Deterministic Neural Network\nUsing LSTMs to Predict Stock Prices\nVision and Deep Learning-Based Algorithms to Detect and Quantify Cracks on Concrete\nWeight Normalization: A Simple Reparameterization to Accelerate Training of Deep Neural Networks\n2021 Fall Projects\nComposing Music With Recurrent Neural Networks\nA New Backbone for Hyperspectral Image Reconstruction\nA Recurrent Latent Variable Model for Sequential Data\nCustom DenseNet models for Tiny ImageNet Classification\nDeep compressive autoencoder for action potential compression in large-scale neural recording\nDeep Networks with Stochastic Depth\nDeepPainter: Painter Classification Using Deep Convolutional Autoencoders\nFairness without Demographics through Adversarially Reweighted Learning\nFishNet: A Versatile Backbone for Image, Region, and Pixel Level Prediction\nMoEL: Mixture of Empathetic Listeners\nNeural Distance Embeddings for Biological Sequences\nNeural Distance Embeddings for Biological Sequences\nPhysics-informed neural networks: A deep learning framework for solving forward and inverse problems involving nonlinear partial differential equations\nQANet: Combining Local Convolution with Global Self-Attention for Reading Comprehension\nResidual Attention Network for Image Classification\nSocial GAN for Human Driving Trajectories Prediction\nSpectral Representations for Convolutional Neural Networks\nSqueeze-and-Excitation Networks Code avaiable\nTowards Accurate Binary Convolutional Neural Network Xiaofan Lin, Cong Zhao, Wei Pan, (2017NIPS)\nVideo-based human emotion recognition\n2021 Spring Projects\nA deep learning framework for financial time series using stacked autoencoders and long-short term memory\n\nA Neural Algorithm of Artistic Style\n\nA neural attention model for speech command recognition\n\nBinaryConnect: Training Deep Neural Networks with Binary Weights during Propagations\n\nComposing Music With Recurrent Neural Networks\n\nConditional Generative Adversarial Net\n\nDeep Double Descent: Where Bigger Models and More Data Hurt\n\nDeep Koalarization: Image Colorization using CNNs and Inception-ResNet-v2\n\nDeep Learning for Price Prediction of Cryptocurrencies\n\nDeepPaint -- Classification of Paintings using Convolutional Autoencoder\n\nDensely Connected Convolutional Networks\n\nEnhancing Detection of Steady-State Visual Evoked Potentials Using Deep Learning\n\nFrom 2D to 3D: Kidney Tumor Segmentation Challenge\n\nGRUV: Algorithmic Music Generation using Recurrent Neural Networks\n\nHighway Networks\n\nMobileNets: Efficient Convolutional Neural Networks for Mobile Vision Applications\n\nNeural Networks for Automated Essay Grading\n\nRecurrent Neural Networks to Create Comprehensive 3D Models of Granular Media in YADE\n\nResidual Attention Network for Image Classification\n\nSinging Voice Separation from Monaural Recordings Using Deep Recurrent Neural Networks\n\nU-Net: Convolutional Networks for Biomedical Image Segmentation\n\nUnsupervised Representation Learning with Deep Convolutional Generative Adversarial Networks\n\n2020 Fall Projects\nCustom DenseNet models for Tiny ImageNet Classification\n\nUnsupervised Representation Learning with Deep Convolutional Generative Adversarial Networks\n\nA neural attention model for speech command recognition\n\nCNN-Generated Images Are Surprisingly Easy to Spot... for Now\n\nComposing Music With Recurrent Neural Networks\n\nCustom DenseNet models for Tiny ImageNet Classification\n\nDeep Compression: Compressing Deep Neural Networks with Pruning, Trained Quantization and Huffman Coding\n\nDeep Learning and the Cross-Section of Stock Returns: Neural Networks Combining Price and Fundamental Information\n\nImage Super-Resolution Using Deep Convolutional Networks\n\nMachine learning methods for crop yield prediction and climate change impact assessment in agriculture\n\nMobileNets: Efficient Convolutional Neural Networks for Mobile Vision Applications\n\nPhysics-informed neural networks: A deep learning framework for solving forward and inverse problems involving nonlinear partial differential equations\n\nPoseCNN: A convolutional Neural Network for 6D Object Pose Estimation in Cluttered Scenes\n\nRandom Erasing Data Augumentation\n\nResidual Attention Network for Image Classification\n\nRethinking Model Scaling for Convolutional Neural Networks - EfficientNet\n\nSemi-Supervised Learning with Graph Convolutional Neural Networks\n\nTime series forecasting of petroleum production using deep LSTM recurrent networks\n\nUnsupervised Representation Learning with Deep Convolutional Generative Adversarial Networks\n\n2019 Projects\nA deep learning framework for financial time series using stacked autoencoders and long-short term memory\n\nAdversarial Autoencoder Assisted Artifact Reduction of Ballistocardiogram in Simultaneous EEG-fMRI Recordings\n\nAdversarial Variational Bayes: Unifying Variational Autoencoders and Generative Adversarial Networks\n\nAutomated Gleason Grading of Prostate Cancer Tissue Microarrays via Deep Learning\n\nDeep learning-based feature engineering for stock price movement prediction\n\nDeformable Convolutional Networks:(Expanding the receptive field through Deformable Convolution)\n\nDevelopment and validation a RNN model to teach machines to read and comprehend\n\nFaster R-CNN: Towards Real-Time Object Detection with Region Proposal Networks\n\nFishNet: A Versatile Backbone for Image, Region, and Pixel Level Prediction\n\nMobileNets: Efficient Convolutional Neural Networks for Mobile Vision Applications\n\nMulti-Digit Number Recognition from Street View Imagery Using Deep Convolutional Neural Networks\n\nOne-Shot Video Object Segmentation for Mobile Vision Applications\n\nPelee: A Real-Time Object Detection System on Mobile Devices\n\nResidual Attention Network for Image Classification\n\nShort Term Electricity Consumption Forecasting in Residential Building with a Selected Auto-regressive Features&ConvLSTM Neural Network Method\n\nShow and Tell: A Neural Image Caption Generator\n\nSSD: Single Shot MultiBox Detector\n\nStock market's price movement prediction with LSTM neural networks\n\nTowards Accurate Binary Convolutional Neural Network\n\nWeight Normalization: A Simple Reparameterization to Accelerate Training of Deep Neural Networks\n\nWhy Should I Trust You? Explaining the Predictions of Any Classifier\n\nWorld Models\n\n2018 Projects\nA deep learning framework for relationship extraction from articles using long-short term memory and named entity recognition\n\nA Neural Algorithm of Artistic Style\n\nA Neural Representation of Sketch Drawings\n\nAdversarial Variational Bayes: Unifying Variational Autoencoders and Generative Adversarial Networks\n\nBackprop KF: Learning Discriminative Deterministic State Estimators\n\nDeep contextualized word representations\n\nDynamic Routing Between Capsules\n\nGesture Recognition\n\nLearned in Translation: Contextualized Word Vectors\n\nLearning a Probabilistic Latent Space of Object Shapes via 3D Generative-Adversarial Modeling\n\nMaximum Classifier Discrepancy for Unsupervised Domain Adaptation\n\nMulti-Digit Number Recognition from Street View Imagery Using Deep Convolutional Neural Networks\n\nNeural Networks for Automated Essay Grading\n\nParallel Multi-Dimensional LSTM,With Application to Fast Biomedical Volumetric Image Segmentation\n\nPixelGAN Autoencoders\n\nPrevention of catastrophic forgetting in Neural Networks for lifelong learning\n\nSemantic Image Inpainting with Deep Generative Models\n\nTowards Accurate Binary Convolutional Neural Network\n\nUniversal Style Transfer via Feature Transforms\n\nUnsupervised Image-to-Image Translation Networks\n\n2016 Projects\nStriving for Simplicity: The All Convolutional Net\n\nA Combined Semi-supervised Learning mechanism for Video Data via Deep Learning\n\nA Neural Algorithm of Artistic Style\n\nAdieu features? End-to-end speech emotion recognition using a deep convolutional recurrent network\n\nColorful Image Colorization\n\nDeep Networks with Stochastic Depth\n\nHighway Networks\n\nImage Super-Resolution Using Deep Convolutional Networks\n\nLearning to Protect Communications with Adversarial Neural Cryptography\n\nSinging Voice Separation from Monaural Recordings Using Deep\n\nRecurrent Neural Networks\n\nSpatial Transformer Networks\n\nSpoken Language Understanding Using Long-Short Term Memory Neural Networks\n\nStriving for Simplicity: The All Convolutional Net\n\nUnsupervised Representation Learning with Deep Convolutional Generative Adversarial Networks\n\nCourse sponsored by equipment and financial contributions of:\nNVidia GPU Education Center, Google Cloud, IBM Bluemix, AWS Educate, Atmel, Broadcom (Wiced platform); Intel (Edison IoT platform), Silicon Labs.\n",
      "tags": [
        "courses"
      ],
      "id": "f27e67b8-0e44-4486-ab89-3cb3a08b87c9",
      "version": 1,
      "weaviate_uuid": "3869f15a-601d-41dd-b1d2-e9b0aab4cccc",
      "created_at": "2026-02-06T19:14:26",
      "updated_at": "2026-02-06T23:06:34",
      "deleted_at": null
    },
    {
      "title": "E6692",
      "content": "# E6692\n\n\nZoran Kostic Columbia Site\t\nHomeCoursesPeopleNewsResearch & TA/CA OpportunitiesPostdocPublications\t\nDeep Learning on the Edge\nEECS E6792 Deep Learning on the Edge\n(previous: EECS E6692 Topics in Data Driven Analysis and Computation  - Deep Learning on the Edge)\nColumbia University Course \nZoran Kostic, Ph.D., Dipl. Ing.,  Professor of Professional Practice, zk2172(at)columbia.edu\nElectrical Engineering Department, Data Sciences Institute, Columbia University in the City of New York\nCourse in a Nutshell\nTheory and Practice of Deep Learning on the Edge, with Labs Using Nvidia Jetson Nano Devices.\n\nDescription\nThis is an advanced-level course with labs in which students build and experiment with deep learning models, implementing them on a low-power GPU edge computing device. The topics covered by the course are: (*) architectures of low power GPU devices; (*) algorithms and DL models suitable for edge implementation; (*) CUDA language; (*) pre-processing to condition the data; (*) labeling for ground-truth annotation; (*) profiling techniques; (*) connectivity between edge devices and cloud computing servers; (*) real-life data for experimentation; (*) comparison of performance for a variety of methods; (*) comparison of performance of edge-computing and cloud computing approaches.\nThe labs will use NVIDIA Jetson Nano as the edge device.\nFrom year to year, the course may focus on a contemporary application, such as a smart city intersection or patient health monitoring.\nThe course is interactive and requires students’ active participation in every session by presenting, interpreting, and implementing deep learning papers and models. The study of concepts is accompanied by lab sessions in which students will bring the models under consideration to life and experiment with them. There will be half a dozen lab sessions. There will be a final project, preceded by a proposal. Final projects need to be documented in a conference-style report, with code deposited in a GitHub repository. The code needs to be documented and instrumented so the instructor can run it after downloading from the repository. A Google Slides presentation of the project (suitable for poster version) is required. A web-hosted record/documentation of both labs and projects may be expected.\nStudents must be self-sufficient learners and ready for hands-on coding. They have to take an active role during the lab activities.\nPrerequisites\n(i) Machine Learning (taken previously for academic credit).\n(ii) Students entering the course have to have prior experience with deep learning and neural network architectures, including Convolutional Neural Nets (CNNs), Recurrent Neural Networks (RNNs), Long Short Term Memories (LSTMs), and autoencoders. They need to have a working knowledge of coding in Python, Python libraries, Jupyter notebook, Tensorflow, both on local machines and on the Google Cloud, and of Bitbucket, Github, or similar.\n(iii) ECBM E4040 Neural Networks and Deep Learning, or an equivalent neural network/DL university course taken for academic credit. Whereas the quality of online ML and DL courses (Coursera, Udacity, edX) is outstanding, many takers of online courses do the hands-on coding assignments superficially and therefore do not gain practical coding skills, which are essential to participation in this advanced course.\n(iv) The course requires an excellent theoretical background in probability and statistics and linear algebra. \nStudents are strongly advised to drop the class if they do not have adequate theoretical background and/or previous experience with programming deep learning models. It is strongly advised (the instructor’s requirement) that students take no more than 12 credits of any coursework (including projects) during the semester while this course is being taken.\nRegistration\nThe enrollment is limited to several dozen students. Instructor’s permission is required to register. Students interested in the course need to register on the SSOL waitlist and may be asked to complete a questionnaire (to be announced). The instructor will move the students off the SSOL waitlist after reviewing the questionnaire.\nContent\nAnalytical study and software design.\n\nSeveral labs in Python and in PyTorch\n\nSignificant project.\n\nPursuing a deeper exploration of deep learning.\n\nOrganization\n\nLectures\n\nLabs (Assignments)\n\nExam and Quizzes\n\nProjects:\n\nTeam-based\n\nStudents with complementary backgrounds\n\nSignificant design\n\nReports and presentations to the Columbia and NYC community\n\nBest projects could qualify for publications and/or funding\n\nPrerequisites:\nRequired: knowledge of linear algebra, probability and statistics, programming, machine learning, first course in deep learning.\n\nPrerequisite courses: Some combination of a first course in Neural Networks/Deep Learning (Columbia University ECBM E4040 or similar), Internet of Things, embedded systems, and projects in neural networks/deep learning or IOT\n\nTime:\nSpring 2026: changed the course number from 6692 to 6792\n\nSpring 2025\n\nSpring 2024\n\nSpring 2023 \n\nSpring 2022 \n\nProject Areas\n\nSmart cities\n\nMedical\n\nAutonomous vehicles\n\nEnvironmental\n\nPhysical data analytics\n\n\nBooks, Tools and Resources\n\nBOOKS:\n\nDeep Learning, Ian Goodfellow, Yoshua Bengio, and Aaron Courville, The MIT Press, 2016, in preparation.\n\nDevelopment platforms and software:\n\nNVIDIA Jetson Nano\n\nPyTorch as the main framework, Google TensorFlow, Google Cloud, Python, Bitbucket\n\nNvidia’s Deep Neural Network Library (cuDNN)\n\n2025 Spring Projects\nAnomaly Detection of Retinal Fundus Images on Jetson Nano\n\nCan we Compress the Smaller LLaMas onto the Jetson Nano?\n\nReal-time financial sentiment analysis based on distilled finBert\n\nMUSE – User-Sensitive musical Expression\n\nReal-time single/multi-person pose estimation on Edge AI devices\n\nEnergy Efficient Edge AI Inference on Jetson Nano\n\nReal-Time Birdsong Recognition on Edge Devices\n\nReal-Time Artist Recognition and Insight Generation (Claude MonAI)\n\nImplement Traffic Sign Detection with Latest YOLO Models\n\nCrosswalk detection on Jetson Nano\n\nReal-Time Hierarchical Visual Localization and SLAM on Jetson Nano\n\nBitNet : Analysis of 1-bit LLMs for Edge device\n\nAutonomous navigation for blind on Jetson Nano\n\nSmart Recycling: Household Waste Classification Using Deep Learning\n\nEmotions at the Edge: Real-Time Detection with Jetson Nano\n\n2024 Spring Projects\nReal-time single/multi-person pose estimation on Edge AI devices\n\nAutoCaption: Generating Engaging Instagram Captions from Photos\n\nPupil Center Based Eye Tracking\n\nTool learning for LLM assistants responding to voice commands\n\nLive-Inference Skin Cancer Detection \n\nLandmark Lens: Real-Time NYC Landmark Recognition with YOLO and DETR Models\n\nListen, Chat, and Edit on Edge: Text-Guided Soundscape Modification for Enhanced Auditory Experience\n\nSports video Analysis on Jetson Nano\n\nMirror and Glass Detection/ Segmentation\n\nLR-Bot: An Autonomous Litter Rescuer\n\nNanoFaceCheck\n\nPlant Classification\n\n2022 Spring Projects\nReal-Time Chess Bot\n\nSign Language Translation\n\nFall Detection\n\nConditional GAN Compression\n\nDeployment of a DL model on Edge Devices\n\nSleep Apnea Detection\n\nObject tracking Using RL\n\nSmall-Footprint Keyword Spotting\n\nConditional GAN Compression\n\nCourse sponsored by equipment and financial contributions of:\nAmazon AWS, NVidia GPU Education Center, Google Cloud, IBM Bluemix, AWS Educate, Atmel, Broadcom (Wiced platform); Intel (Edison IoT platform), Silicon Labs.\n",
      "tags": [
        "courses"
      ],
      "id": "7f531096-a365-48bd-bec5-7684b898eaa2",
      "version": 1,
      "weaviate_uuid": "ecc3442a-bd5f-48e8-b550-961ba75e14a6",
      "created_at": "2026-02-06T19:36:45",
      "updated_at": "2026-02-06T23:06:34",
      "deleted_at": null
    },
    {
      "title": "test auto save",
      "content": "111222",
      "tags": [
        "text"
      ],
      "id": "a2bb18f4-1153-4fd6-b1ef-c3aacdd8f248",
      "version": 4,
      "weaviate_uuid": "cb92ca36-350f-408e-9bf2-dc4c84c316ce",
      "created_at": "2026-02-06T21:38:43",
      "updated_at": "2026-02-06T23:06:34",
      "deleted_at": null
    },
    {
      "title": "string",
      "content": "string",
      "tags": [
        "string"
      ],
      "id": "a614e40f-0f8d-45a6-a03e-2fdf7e84fe75",
      "version": 1,
      "weaviate_uuid": "e8456a69-83da-4ce2-a7f2-3a4387ec42fa",
      "created_at": "2026-02-06T22:05:04",
      "updated_at": "2026-02-06T23:06:34",
      "deleted_at": null
    }
  ],
  "total": 6,
  "page": 1,
  "limit": 50
}
```

### Get Note by ID

Retrieve a single note.

**Endpoint:** `GET /api/v1/notes/{note_id}`

**Windows Client Example:**
```typescript
// windows-client/src/services/api.ts:60-63
const response = await api.get<Note>(`/notes/${a614e40f-0f8d-45a6-a03e-2fdf7e84fe75}`)
return response.data
```

**Response:**
```json
{
  "title": "string",
  "content": "string",
  "tags": [
    "string"
  ],
  "id": "a614e40f-0f8d-45a6-a03e-2fdf7e84fe75",
  "version": 1,
  "weaviate_uuid": "be85a490-2236-4e07-a6bf-fd3f343f1c5d",
  "created_at": "2026-02-06T22:05:04",
  "updated_at": "2026-02-06T23:57:35",
  "deleted_at": null
}
```

### Create Note

Create a new note.

**Endpoint:** `POST /api/v1/notes`

**Windows Client Example:**
```typescript
// windows-client/src/services/api.ts:65-68
const response = await api.post<Note>('/notes', {
  "title": "New Note 4444",
  "content": "Note content here",
  "tags": ["personal", "ideas"]
})
return response.data
```

**Request:**
```json
{
  "title": "New Note 4444",
  "content": "Note content here",
  "tags": [
    "personal",
    "ideas"
  ],
  "id": "3a395258-e5c7-4919-83f5-80cb601fede6",
  "version": 1,
  "weaviate_uuid": "4017bf7d-3f8f-42eb-b50e-72e6f7547fce",
  "created_at": "2026-02-06T23:51:02",
  "updated_at": "2026-02-06T23:51:04",
  "deleted_at": null
}
```

**Response:** Same as Get Note (201 Created)

### Update Note

Update an existing note. Requires version number for conflict detection.

**Endpoint:** `PUT /api/v1/notes/{note_id}`

**Windows Client Example:**
```typescript
// windows-client/src/services/api.ts:77-80
const response = await api.put<Note>(`/notes/${06ff02b3-d1d3-434d-ab50-657fcbce135f}`, {
  title: "Updated Title",
  content: "Updated content",
  tags: ["updated"],
  version: 1  // Current version for conflict detection
})
```

**Request:**
```json
{
  "title": "Updated Title",
  "content": "Updated content",
  "tags": [
    "updated"
  ],
  "id": "a614e40f-0f8d-45a6-a03e-2fdf7e84fe75",
  "version": 2,
  "weaviate_uuid": "51997555-d724-4d01-aafb-029f9939d977",
  "created_at": "2026-02-06T22:05:04",
  "updated_at": "2026-02-06T23:59:25.283407",
  "deleted_at": null
}
```

**Response:** Updated note object

**Note:** If the version doesn't match (someone else updated it), you'll get a 409 Conflict error.

### Delete Note

Soft delete a note (marks as deleted but doesn't remove from database).

**Endpoint:** `DELETE /api/v1/notes/{note_id}`

**Windows Client Example:**
```typescript
// windows-client/src/services/api.ts:82-84
await api.delete(`/notes/${b8700f8a-91e8-445b-8907-a33933aec412}`)
```

**Response:** 204 No Content

### List Tags

Get all unique tags with usage counts.

**Endpoint:** `GET /api/v1/notes/tags`

**Response:**
```json
{
  "tags": [
    {
      "tag": "courses",
      "count": 2
    },
    {
      "tag": "ideas",
      "count": 1
    },
    {
      "tag": "personal",
      "count": 1
    },
    {
      "tag": "string",
      "count": 1
    },
    {
      "tag": "text",
      "count": 1
    }
  ]
}
```

---

## Search API

### Semantic Search

Search notes using AI-powered vector similarity (powered by Weaviate).

**Endpoint:** `GET /api/v1/notes/search/semantic`

**Query Parameters:**
- `q` (string, required) - Search query
- `limit` (integer, default: 5, max: 20) - Maximum results
- `tags` (string, optional) - Comma-separated tags filter

**Windows Client Example:**
```typescript
// windows-client/src/services/api.ts:70-75
const response = await api.get<{ results: SemanticSearchResult[] }>(
  '/notes/search/semantic',
  {
    params: { q: "machine learning concepts", limit: 10 }
  }
)
return response.data.results
```

**Response:**
```json
{
  "results": [
    {
      "note_id": "f27e67b8-0e44-4486-ab89-3cb3a08b87c9",
      "title": "E4040",
      "content": "ing Shifted Windows\nSwin Transformer: Hierarchical Vision Transformer Using Shifted Windows\nUncertainty Estimation Using a Single Deep Deterministic Neural Network\nUsing LSTMs to Predict Stock Prices\nVision and Deep Learning-Based Algorithms to Detect and Quantify Cracks on Concrete\nWeight Normalization: A Simple Reparameterization to Accelerate Training of Deep Neural Networks\n2021 Fall Projects\nComposing Music With Recurrent Neural Networks\nA New Backbone for Hyperspectral Image Reconstruction\nA Recurrent Latent Variable Model for Sequential Data\nCustom DenseNet models for Tiny ImageNet Classification\nDeep compressive autoencoder for action potential compression in large-scale neural recording\nDeep Networks with Stochastic Depth\nDeepPainter: Painter Classification Using Deep Convolutional Autoencoders\nFairness without Demographics through Adversarially Reweighted Learning\nFishNet: A Versatile Backbone for Image, Region, and Pixel Level Prediction\nMoEL: Mixture of Empathetic Listeners\nNeural Distance Embeddings for Biological Sequences\nNeural Distance Embeddings for Biological Sequences\nPhysics-informed neural networks: A deep learning framework for solving forward and inverse problems involving nonlinear partial differential equations\nQANet: Combining Local Convolution with Global Self-Attention for Reading Comprehension\nResidual Attention Network for Image Classification\nSocial GAN for Human Driving Trajectories Prediction",
      "tags": [
        "courses"
      ],
      "created_at": "2026-02-06T19:14:26",
      "updated_at": "2026-02-06T23:46:17",
      "relevance_score": 0.6196260452270508
    },
    {
      "note_id": "b8700f8a-91e8-445b-8907-a33933aec412",
      "title": "hello",
      "content": "# hello\n\nhello hello hello\n",
      "tags": [],
      "created_at": "2026-02-06T19:12:23",
      "updated_at": "2026-02-06T23:46:17",
      "relevance_score": 0.46082937717437744
    }
  ],
  "query": "machine learning concepts",
  "count": 2
}
```

### Search Within Note

Search within a specific note and get context around the best match.

**Endpoint:** `GET /api/v1/notes/{note_id}/search`

**Query Parameters:**
- `q` (string, required) - Search query
- `window` (integer, default: 2, max: 5) - Number of chunks before/after best match

**Windows Client Example:**
```typescript
// windows-client/src/services/api.ts:91-102
const response = await api.get<{
  context: string;
  title: string;
  chunk_range: string;
  total_chunks: number;
  best_chunk_index: number;
}>(`/notes/${b8700f8a-91e8-445b-8907-a33933aec412}/search`, {
  params: { q: "specific paragraph", window: 2 }
})
```

**Response:**
```json
{
  "context": "# hello\n\nhello hello hello\n",
  "title": "hello",
  "chunk_range": "0-0",
  "total_chunks": 1,
  "best_chunk_index": 0
}
```

### Reindex Notes

Re-index all notes in Weaviate (useful after bulk changes).

**Endpoint:** `POST /api/v1/notes/reindex`

**Windows Client Example:**
```typescript
// windows-client/src/services/api.ts:86-89
const response = await api.post<{
  message: string;
  total: number;
  success: number;
  errors: number;
}>('/notes/reindex')
```

**Response:**
```json
{
  "message": "Reindexed 8 notes successfully",
  "total": 8,
  "success": 8,
  "errors": 0
}
```

---

## Chat API (RAG)

### Chat Query

Ask questions about your notes using Retrieval-Augmented Generation (RAG).

**Endpoint:** `POST /api/v1/chat/query`

**Windows Client Example:**
```typescript
// windows-client/src/services/api.ts:106-120
const response = await api.post<ChatResponse>('/chat/query', {
  "message": "What are my notes about machine learning?",
  "session_id": "session-123",  // Optional, for conversation continuity
  "limit": 5,
  "additional_context": "Focus on practical applications"  // Optional
})
```

**Request:**
```json
{
  "message": "What are my notes about machine learning?",
  "limit": 5,
  "additional_context": "Focus on practical applications"
}
```

**Response:**
```json
{
  "message": "Based on your notes, you have several entries about machine learning...",
  "session_id": "550e8400-e29b-41d4-a716-446655440000",
  "context_notes": [
    {
      "note_id": "123e4567-e89b-12d3-a456-426614174000",
      "title": "ML Basics",
      "content_preview": "Machine learning is a subset of AI...",
      "relevance_score": 0.92
    }
  ],
  "created_at": "2026-02-06T10:30:00Z"
}
```

### Get Chat History

Retrieve conversation history for a session.

**Endpoint:** `GET /api/v1/chat/history`

**Query Parameters:**
- `session_id` (string, required) - Session UUID
- `limit` (integer, default: 50, max: 200) - Maximum messages

**Response:**
```json
{
  "messages": [],
  "session_id": "550e8400-e29b-41d4-a716-446655440000",
  "total": 0
}
```

### Create Chat Session

Create a new chat session.

**Endpoint:** `POST /api/v1/chat/sessions`

**Response:**
```json
{
  "session_id": "e8b15582-2549-43f7-a97c-c87cc17c19f9"
}
```

---

## Sync API

Used for synchronizing notes between devices.

### Sync Notes

Batch sync notes between client and server.

**Endpoint:** `POST /api/v1/sync`

**Request:**
```json
{
  "device_id": "device-uuid",
  "last_sync_timestamp": "2026-02-06T09:00:00Z",
  "notes": [
    {
      "id": "note-uuid",
      "version": 2,
      "action": "update",
      "data": {
        "title": "Updated Note",
        "content": "New content",
        "tags": ["updated"]
      }
    },
    {
      "id": "note-uuid-2",
      "version": 1,
      "action": "delete",
      "data": null
    }
  ]
}
```

**Response:**
```json
{
  "notes_to_update": [
    {
      "title": "E4040",
      "content": "# E4040\n\n\nZoran Kostic Columbia Site\t\nHomeCoursesPeopleNewsResearch & TA/CA OpportunitiesPostdocPublications\t\nNeural Networks and Deep Learning\nNeural Networks and Deep Learning\nColumbia University course ECBM E4040\nZoran Kostic, Ph.D., Dipl. Ing.,  Professor of Professional Practice, zk2172(at)columbia.edu\nElectrical Engineering Department, Data Sciences Institute,  Columbia University in the City of New York\nCourse in a nutshell: \n Theoretical underpinnings and practical aspects of Neural Networks and Deep Learning. Convolutional and Recurrent Neural Networks. Focus on applications and projects.\nBulletin Description:\nDeveloping features & internal representations of the world, artificial neural networks, classifying handwritten digits with logistics regression, feedforward deep networks, back propagation in multilayer perceptrons, regularization of deep or distributed models, optimization for training deep models, convolutional neural networks, recurrent and recursive neural networks, deep learning in speech and object recognition.\n\nRegistration:\nTo register into the course: (i) students need to get onto the Columbia SSOL waitlist, and (ii) populate this form (using CU UNI email). Those students who satisfy the requirements will be moved from the waitlist into the registered list by early September. The course can be taken by undergraduate and graduate student. \nPrerequisites:\nRequired prerequisites: knowledge of linear algebra, probability and statistics, programming. Machine learning must be taken as an academic course either before or in parallel to this course.\n\nOrganization\nLectures:\n\nPresentation of material by instructors and guest lecturers\n\nAssignments:\n\nCombination of analytical and programming assignments\n\nExam and quizzes.\n\nProjects:\n\nTeam-based\n\nStudents with complementary backgrounds\n\nEngineering report\n\nContent\nIntroduction to neural networks.\n\nConvolutional and recurrent neural networks.\n\nFocuses on the intuitive understanding of deep learning.\n\nReview of underpinning theory - linear algebra, statistics, machine learning.\n\nAnalytical study and software design.\n\nThree-four assignments in Python and one DL framework (Tensorflow or PyTorch)\n\nSignificant project.\n\nEnables further exploration of key concepts in deep learning.\n\nTypical Syllabus\nIntroduction to Course E4040\n\nIntroduction to Deep Learning (DL)\n\nIntroduction to DL computing Resources\n\nMachine Learning Algorithms\n\nt-SNE Data Visualization\n\nUniversal approximation theorem - Visual proof\n\nAlgebra review \n\nDeep Feed-forward Networks\n\nBack Propagation\n\nOptimization\n\nConvolutional Neural Nets (CNNs)\n\nCNN applications\n\nCNN Examples\n\nRegularization\n\nPractical Methodology for Deep Learning\n\nRecurrent Neural Nets (RNNs)\n\nRNN applications\n\nDeel Learning Applications\n\nAutoencoders\n\nGenerative Networks GANs, Variational Encoders\n\nGenerative Models\n\nGuest Lectures\n\nDeep Learning Trends\n\nBooks, Tools and Resources\nBOOKS:\n\nDeep Learning, Ian Goodfellow, Yoshua Bengio, and Aaron Courville, The MIT Press, 2016.\n\n2017-2026 software platform:\n\nGoogle TensorFlow, Google Cloud (GCP), Python, Github, Google Colab\n\n2016 software platform:\n\nTheano\n\nNvidia’s Deep Neural Network Library (cuDNN)\n\nAmazon AWS cloud tools, Code development on github, bitbucket\n\nProject Areas\nMedical\n\nAutonomous cars\n\nEnvironmental\n\nSmart cities\n\nPhysical data analytics\n\n2024 Fall Projects\nConvMixer: Patches are all you need?\nAntibody-Antigen Interaction Prediction with Modified CNN Model and Encoding Comparisons\nAttention-Guided Version of 2D UNet for Automatic Brain Tumor Segmentation\nConvMixer: Patches are all you need?\nDeep Clustering for Unsupervised Learning of Visual Features\nDensely Connected Convolutional Networks\nGlobal Filter Networks for Image Classiﬁcation\nImproving CNN Robustness via CS Shapley Value-guided Augmentation\nKAN: Kolmogorov–Arnold Networks \nLSTM Fully Convolutional Networks for Time Series Classification\nResidual Attention Network for Image Classification\nShow, Attend and Tell: Neural Image Caption Generation with Visual Attention\nSimCLR - A Simple Framework for Contrastive Learning of Visual Representations\nSwin Transformer: Hierarchical Vision Transformer Using Shifted Windows\nUsing DUCK-Net for Polyp Image Segmentation\nWalsh-Hadamard Transforms in Neural Networks\n2022 Fall Projects\nBinaryConnect: Training Deep Neural Networks with binary weights during propagations\nApplication of transformer model for the time series forecast\nDeep Learning for Symbolic Mathematics\nImage Demoireing with Learnable Bandpass Filters\nIoU comparison: DIoU, CIoU\nJoint Face Detection and Alightment using Multi-task Cascaded CNN\nMobileNets: Efficient Convolutional Neural Networks for Mobile Vision Applications\nOptical Coherence Tomography Enabled Classification of the Pulmonary Vein\nRegistration of Optical Coherence Tomography Volumes Subject to Rotation, Translation and Occlusion\nSinging Voice Separation from Monaural Recordings Using Deep Recurrent Neural Networks\nSingle-Image Depth Perception in the Wild\nSpectral Representations for Convolutional Neural Networks\nSwin Transformer: Hierarchical Vision Transformer Using Shifted Windows\nSwin Transformer: Hierarchical Vision Transformer Using Shifted Windows\nUncertainty Estimation Using a Single Deep Deterministic Neural Network\nUsing LSTMs to Predict Stock Prices\nVision and Deep Learning-Based Algorithms to Detect and Quantify Cracks on Concrete\nWeight Normalization: A Simple Reparameterization to Accelerate Training of Deep Neural Networks\n2021 Fall Projects\nComposing Music With Recurrent Neural Networks\nA New Backbone for Hyperspectral Image Reconstruction\nA Recurrent Latent Variable Model for Sequential Data\nCustom DenseNet models for Tiny ImageNet Classification\nDeep compressive autoencoder for action potential compression in large-scale neural recording\nDeep Networks with Stochastic Depth\nDeepPainter: Painter Classification Using Deep Convolutional Autoencoders\nFairness without Demographics through Adversarially Reweighted Learning\nFishNet: A Versatile Backbone for Image, Region, and Pixel Level Prediction\nMoEL: Mixture of Empathetic Listeners\nNeural Distance Embeddings for Biological Sequences\nNeural Distance Embeddings for Biological Sequences\nPhysics-informed neural networks: A deep learning framework for solving forward and inverse problems involving nonlinear partial differential equations\nQANet: Combining Local Convolution with Global Self-Attention for Reading Comprehension\nResidual Attention Network for Image Classification\nSocial GAN for Human Driving Trajectories Prediction\nSpectral Representations for Convolutional Neural Networks\nSqueeze-and-Excitation Networks Code avaiable\nTowards Accurate Binary Convolutional Neural Network Xiaofan Lin, Cong Zhao, Wei Pan, (2017NIPS)\nVideo-based human emotion recognition\n2021 Spring Projects\nA deep learning framework for financial time series using stacked autoencoders and long-short term memory\n\nA Neural Algorithm of Artistic Style\n\nA neural attention model for speech command recognition\n\nBinaryConnect: Training Deep Neural Networks with Binary Weights during Propagations\n\nComposing Music With Recurrent Neural Networks\n\nConditional Generative Adversarial Net\n\nDeep Double Descent: Where Bigger Models and More Data Hurt\n\nDeep Koalarization: Image Colorization using CNNs and Inception-ResNet-v2\n\nDeep Learning for Price Prediction of Cryptocurrencies\n\nDeepPaint -- Classification of Paintings using Convolutional Autoencoder\n\nDensely Connected Convolutional Networks\n\nEnhancing Detection of Steady-State Visual Evoked Potentials Using Deep Learning\n\nFrom 2D to 3D: Kidney Tumor Segmentation Challenge\n\nGRUV: Algorithmic Music Generation using Recurrent Neural Networks\n\nHighway Networks\n\nMobileNets: Efficient Convolutional Neural Networks for Mobile Vision Applications\n\nNeural Networks for Automated Essay Grading\n\nRecurrent Neural Networks to Create Comprehensive 3D Models of Granular Media in YADE\n\nResidual Attention Network for Image Classification\n\nSinging Voice Separation from Monaural Recordings Using Deep Recurrent Neural Networks\n\nU-Net: Convolutional Networks for Biomedical Image Segmentation\n\nUnsupervised Representation Learning with Deep Convolutional Generative Adversarial Networks\n\n2020 Fall Projects\nCustom DenseNet models for Tiny ImageNet Classification\n\nUnsupervised Representation Learning with Deep Convolutional Generative Adversarial Networks\n\nA neural attention model for speech command recognition\n\nCNN-Generated Images Are Surprisingly Easy to Spot... for Now\n\nComposing Music With Recurrent Neural Networks\n\nCustom DenseNet models for Tiny ImageNet Classification\n\nDeep Compression: Compressing Deep Neural Networks with Pruning, Trained Quantization and Huffman Coding\n\nDeep Learning and the Cross-Section of Stock Returns: Neural Networks Combining Price and Fundamental Information\n\nImage Super-Resolution Using Deep Convolutional Networks\n\nMachine learning methods for crop yield prediction and climate change impact assessment in agriculture\n\nMobileNets: Efficient Convolutional Neural Networks for Mobile Vision Applications\n\nPhysics-informed neural networks: A deep learning framework for solving forward and inverse problems involving nonlinear partial differential equations\n\nPoseCNN: A convolutional Neural Network for 6D Object Pose Estimation in Cluttered Scenes\n\nRandom Erasing Data Augumentation\n\nResidual Attention Network for Image Classification\n\nRethinking Model Scaling for Convolutional Neural Networks - EfficientNet\n\nSemi-Supervised Learning with Graph Convolutional Neural Networks\n\nTime series forecasting of petroleum production using deep LSTM recurrent networks\n\nUnsupervised Representation Learning with Deep Convolutional Generative Adversarial Networks\n\n2019 Projects\nA deep learning framework for financial time series using stacked autoencoders and long-short term memory\n\nAdversarial Autoencoder Assisted Artifact Reduction of Ballistocardiogram in Simultaneous EEG-fMRI Recordings\n\nAdversarial Variational Bayes: Unifying Variational Autoencoders and Generative Adversarial Networks\n\nAutomated Gleason Grading of Prostate Cancer Tissue Microarrays via Deep Learning\n\nDeep learning-based feature engineering for stock price movement prediction\n\nDeformable Convolutional Networks:(Expanding the receptive field through Deformable Convolution)\n\nDevelopment and validation a RNN model to teach machines to read and comprehend\n\nFaster R-CNN: Towards Real-Time Object Detection with Region Proposal Networks\n\nFishNet: A Versatile Backbone for Image, Region, and Pixel Level Prediction\n\nMobileNets: Efficient Convolutional Neural Networks for Mobile Vision Applications\n\nMulti-Digit Number Recognition from Street View Imagery Using Deep Convolutional Neural Networks\n\nOne-Shot Video Object Segmentation for Mobile Vision Applications\n\nPelee: A Real-Time Object Detection System on Mobile Devices\n\nResidual Attention Network for Image Classification\n\nShort Term Electricity Consumption Forecasting in Residential Building with a Selected Auto-regressive Features&ConvLSTM Neural Network Method\n\nShow and Tell: A Neural Image Caption Generator\n\nSSD: Single Shot MultiBox Detector\n\nStock market's price movement prediction with LSTM neural networks\n\nTowards Accurate Binary Convolutional Neural Network\n\nWeight Normalization: A Simple Reparameterization to Accelerate Training of Deep Neural Networks\n\nWhy Should I Trust You? Explaining the Predictions of Any Classifier\n\nWorld Models\n\n2018 Projects\nA deep learning framework for relationship extraction from articles using long-short term memory and named entity recognition\n\nA Neural Algorithm of Artistic Style\n\nA Neural Representation of Sketch Drawings\n\nAdversarial Variational Bayes: Unifying Variational Autoencoders and Generative Adversarial Networks\n\nBackprop KF: Learning Discriminative Deterministic State Estimators\n\nDeep contextualized word representations\n\nDynamic Routing Between Capsules\n\nGesture Recognition\n\nLearned in Translation: Contextualized Word Vectors\n\nLearning a Probabilistic Latent Space of Object Shapes via 3D Generative-Adversarial Modeling\n\nMaximum Classifier Discrepancy for Unsupervised Domain Adaptation\n\nMulti-Digit Number Recognition from Street View Imagery Using Deep Convolutional Neural Networks\n\nNeural Networks for Automated Essay Grading\n\nParallel Multi-Dimensional LSTM,With Application to Fast Biomedical Volumetric Image Segmentation\n\nPixelGAN Autoencoders\n\nPrevention of catastrophic forgetting in Neural Networks for lifelong learning\n\nSemantic Image Inpainting with Deep Generative Models\n\nTowards Accurate Binary Convolutional Neural Network\n\nUniversal Style Transfer via Feature Transforms\n\nUnsupervised Image-to-Image Translation Networks\n\n2016 Projects\nStriving for Simplicity: The All Convolutional Net\n\nA Combined Semi-supervised Learning mechanism for Video Data via Deep Learning\n\nA Neural Algorithm of Artistic Style\n\nAdieu features? End-to-end speech emotion recognition using a deep convolutional recurrent network\n\nColorful Image Colorization\n\nDeep Networks with Stochastic Depth\n\nHighway Networks\n\nImage Super-Resolution Using Deep Convolutional Networks\n\nLearning to Protect Communications with Adversarial Neural Cryptography\n\nSinging Voice Separation from Monaural Recordings Using Deep\n\nRecurrent Neural Networks\n\nSpatial Transformer Networks\n\nSpoken Language Understanding Using Long-Short Term Memory Neural Networks\n\nStriving for Simplicity: The All Convolutional Net\n\nUnsupervised Representation Learning with Deep Convolutional Generative Adversarial Networks\n\nCourse sponsored by equipment and financial contributions of:\nNVidia GPU Education Center, Google Cloud, IBM Bluemix, AWS Educate, Atmel, Broadcom (Wiced platform); Intel (Edison IoT platform), Silicon Labs.\n",
      "tags": [
        "courses"
      ],
      "id": "f27e67b8-0e44-4486-ab89-3cb3a08b87c9",
      "version": 1,
      "weaviate_uuid": "9bddc0c5-d653-4487-9baa-8af2fdc713ee",
      "created_at": "2026-02-06T19:14:26",
      "updated_at": "2026-02-06T23:59:13",
      "deleted_at": null
    },
    {
      "title": "E6692",
      "content": "# E6692\n\n\nZoran Kostic Columbia Site\t\nHomeCoursesPeopleNewsResearch & TA/CA OpportunitiesPostdocPublications\t\nDeep Learning on the Edge\nEECS E6792 Deep Learning on the Edge\n(previous: EECS E6692 Topics in Data Driven Analysis and Computation  - Deep Learning on the Edge)\nColumbia University Course \nZoran Kostic, Ph.D., Dipl. Ing.,  Professor of Professional Practice, zk2172(at)columbia.edu\nElectrical Engineering Department, Data Sciences Institute, Columbia University in the City of New York\nCourse in a Nutshell\nTheory and Practice of Deep Learning on the Edge, with Labs Using Nvidia Jetson Nano Devices.\n\nDescription\nThis is an advanced-level course with labs in which students build and experiment with deep learning models, implementing them on a low-power GPU edge computing device. The topics covered by the course are: (*) architectures of low power GPU devices; (*) algorithms and DL models suitable for edge implementation; (*) CUDA language; (*) pre-processing to condition the data; (*) labeling for ground-truth annotation; (*) profiling techniques; (*) connectivity between edge devices and cloud computing servers; (*) real-life data for experimentation; (*) comparison of performance for a variety of methods; (*) comparison of performance of edge-computing and cloud computing approaches.\nThe labs will use NVIDIA Jetson Nano as the edge device.\nFrom year to year, the course may focus on a contemporary application, such as a smart city intersection or patient health monitoring.\nThe course is interactive and requires students’ active participation in every session by presenting, interpreting, and implementing deep learning papers and models. The study of concepts is accompanied by lab sessions in which students will bring the models under consideration to life and experiment with them. There will be half a dozen lab sessions. There will be a final project, preceded by a proposal. Final projects need to be documented in a conference-style report, with code deposited in a GitHub repository. The code needs to be documented and instrumented so the instructor can run it after downloading from the repository. A Google Slides presentation of the project (suitable for poster version) is required. A web-hosted record/documentation of both labs and projects may be expected.\nStudents must be self-sufficient learners and ready for hands-on coding. They have to take an active role during the lab activities.\nPrerequisites\n(i) Machine Learning (taken previously for academic credit).\n(ii) Students entering the course have to have prior experience with deep learning and neural network architectures, including Convolutional Neural Nets (CNNs), Recurrent Neural Networks (RNNs), Long Short Term Memories (LSTMs), and autoencoders. They need to have a working knowledge of coding in Python, Python libraries, Jupyter notebook, Tensorflow, both on local machines and on the Google Cloud, and of Bitbucket, Github, or similar.\n(iii) ECBM E4040 Neural Networks and Deep Learning, or an equivalent neural network/DL university course taken for academic credit. Whereas the quality of online ML and DL courses (Coursera, Udacity, edX) is outstanding, many takers of online courses do the hands-on coding assignments superficially and therefore do not gain practical coding skills, which are essential to participation in this advanced course.\n(iv) The course requires an excellent theoretical background in probability and statistics and linear algebra. \nStudents are strongly advised to drop the class if they do not have adequate theoretical background and/or previous experience with programming deep learning models. It is strongly advised (the instructor’s requirement) that students take no more than 12 credits of any coursework (including projects) during the semester while this course is being taken.\nRegistration\nThe enrollment is limited to several dozen students. Instructor’s permission is required to register. Students interested in the course need to register on the SSOL waitlist and may be asked to complete a questionnaire (to be announced). The instructor will move the students off the SSOL waitlist after reviewing the questionnaire.\nContent\nAnalytical study and software design.\n\nSeveral labs in Python and in PyTorch\n\nSignificant project.\n\nPursuing a deeper exploration of deep learning.\n\nOrganization\n\nLectures\n\nLabs (Assignments)\n\nExam and Quizzes\n\nProjects:\n\nTeam-based\n\nStudents with complementary backgrounds\n\nSignificant design\n\nReports and presentations to the Columbia and NYC community\n\nBest projects could qualify for publications and/or funding\n\nPrerequisites:\nRequired: knowledge of linear algebra, probability and statistics, programming, machine learning, first course in deep learning.\n\nPrerequisite courses: Some combination of a first course in Neural Networks/Deep Learning (Columbia University ECBM E4040 or similar), Internet of Things, embedded systems, and projects in neural networks/deep learning or IOT\n\nTime:\nSpring 2026: changed the course number from 6692 to 6792\n\nSpring 2025\n\nSpring 2024\n\nSpring 2023 \n\nSpring 2022 \n\nProject Areas\n\nSmart cities\n\nMedical\n\nAutonomous vehicles\n\nEnvironmental\n\nPhysical data analytics\n\n\nBooks, Tools and Resources\n\nBOOKS:\n\nDeep Learning, Ian Goodfellow, Yoshua Bengio, and Aaron Courville, The MIT Press, 2016, in preparation.\n\nDevelopment platforms and software:\n\nNVIDIA Jetson Nano\n\nPyTorch as the main framework, Google TensorFlow, Google Cloud, Python, Bitbucket\n\nNvidia’s Deep Neural Network Library (cuDNN)\n\n2025 Spring Projects\nAnomaly Detection of Retinal Fundus Images on Jetson Nano\n\nCan we Compress the Smaller LLaMas onto the Jetson Nano?\n\nReal-time financial sentiment analysis based on distilled finBert\n\nMUSE – User-Sensitive musical Expression\n\nReal-time single/multi-person pose estimation on Edge AI devices\n\nEnergy Efficient Edge AI Inference on Jetson Nano\n\nReal-Time Birdsong Recognition on Edge Devices\n\nReal-Time Artist Recognition and Insight Generation (Claude MonAI)\n\nImplement Traffic Sign Detection with Latest YOLO Models\n\nCrosswalk detection on Jetson Nano\n\nReal-Time Hierarchical Visual Localization and SLAM on Jetson Nano\n\nBitNet : Analysis of 1-bit LLMs for Edge device\n\nAutonomous navigation for blind on Jetson Nano\n\nSmart Recycling: Household Waste Classification Using Deep Learning\n\nEmotions at the Edge: Real-Time Detection with Jetson Nano\n\n2024 Spring Projects\nReal-time single/multi-person pose estimation on Edge AI devices\n\nAutoCaption: Generating Engaging Instagram Captions from Photos\n\nPupil Center Based Eye Tracking\n\nTool learning for LLM assistants responding to voice commands\n\nLive-Inference Skin Cancer Detection \n\nLandmark Lens: Real-Time NYC Landmark Recognition with YOLO and DETR Models\n\nListen, Chat, and Edit on Edge: Text-Guided Soundscape Modification for Enhanced Auditory Experience\n\nSports video Analysis on Jetson Nano\n\nMirror and Glass Detection/ Segmentation\n\nLR-Bot: An Autonomous Litter Rescuer\n\nNanoFaceCheck\n\nPlant Classification\n\n2022 Spring Projects\nReal-Time Chess Bot\n\nSign Language Translation\n\nFall Detection\n\nConditional GAN Compression\n\nDeployment of a DL model on Edge Devices\n\nSleep Apnea Detection\n\nObject tracking Using RL\n\nSmall-Footprint Keyword Spotting\n\nConditional GAN Compression\n\nCourse sponsored by equipment and financial contributions of:\nAmazon AWS, NVidia GPU Education Center, Google Cloud, IBM Bluemix, AWS Educate, Atmel, Broadcom (Wiced platform); Intel (Edison IoT platform), Silicon Labs.\n",
      "tags": [
        "courses"
      ],
      "id": "7f531096-a365-48bd-bec5-7684b898eaa2",
      "version": 1,
      "weaviate_uuid": "bda0d92c-9a22-4b65-8a49-3a3af9a7b95f",
      "created_at": "2026-02-06T19:36:45",
      "updated_at": "2026-02-06T23:59:13",
      "deleted_at": null
    },
    {
      "title": "test auto save",
      "content": "111222",
      "tags": [
        "text"
      ],
      "id": "a2bb18f4-1153-4fd6-b1ef-c3aacdd8f248",
      "version": 4,
      "weaviate_uuid": "fa033e83-2c55-4a51-bb8a-695c52f9e1b2",
      "created_at": "2026-02-06T21:38:43",
      "updated_at": "2026-02-06T23:59:13",
      "deleted_at": null
    },
    {
      "title": "Uploaded notes",
      "content": "text to upload\n\nlocal text local text local text local text local text\nlocal text local text local text local text local text\nlocal text local text local text local text local text\nlocal text local text local text local text local text\nlocal text local text local text local text local text\nlocal text local text local text local text local text\n",
      "tags": [],
      "id": "443ac06d-8b74-481d-bd7a-2523d4ae9da6",
      "version": 1,
      "weaviate_uuid": "0ff10af9-a7e8-4bb9-9168-b8be0fd0d5e5",
      "created_at": "2026-02-06T23:18:04",
      "updated_at": "2026-02-06T23:59:13",
      "deleted_at": null
    },
    {
      "title": "New Note 4444",
      "content": "Note content here",
      "tags": [
        "personal",
        "ideas"
      ],
      "id": "3a395258-e5c7-4919-83f5-80cb601fede6",
      "version": 1,
      "weaviate_uuid": "dee3c89e-b49e-4aed-a645-003b2767dced",
      "created_at": "2026-02-06T23:51:02",
      "updated_at": "2026-02-06T23:59:13",
      "deleted_at": null
    },
    {
      "title": "Updated Title",
      "content": "Updated content",
      "tags": [
        "updated"
      ],
      "id": "a614e40f-0f8d-45a6-a03e-2fdf7e84fe75",
      "version": 2,
      "weaviate_uuid": "51997555-d724-4d01-aafb-029f9939d977",
      "created_at": "2026-02-06T22:05:04",
      "updated_at": "2026-02-06T23:59:25.283407",
      "deleted_at": null
    }
  ],
  "notes_to_delete": [
    "fda6b550-ab82-4350-a568-1527524b8181",
    "8755e2ec-b359-46cf-b02d-b02b797a7871",
    "2cf5fac9-b54a-43d2-85b1-4af546d7e511",
    "06ff02b3-d1d3-434d-ab50-657fcbce135f",
    "b8700f8a-91e8-445b-8907-a33933aec412"
  ],
  "conflicts": [],
  "server_timestamp": "2026-02-07T00:11:28.416115"
}
```

---

## Error Responses

All endpoints follow standard HTTP status codes:

### 400 Bad Request
```json
{
  "detail": "Invalid request parameters"
}
```

### 401 Unauthorized
```json
{
  "detail": "Invalid password"
}
```

### 404 Not Found
```json
{
  "detail": "Note not found"
}
```

### 409 Conflict
```json
{
  "detail": "Version conflict - note was modified by another client"
}
```

### 422 Validation Error
```json
{
  "detail": [
    {
      "loc": ["body", "title"],
      "msg": "field required",
      "type": "value_error.missing"
    }
  ]
}
```

---

## Configuration

### Environment Variables

Key environment variables you can configure:

| Variable | Default | Description |
|----------|---------|-------------|
| `SIMPLE_PASSWORD` | `hexanote` | Authentication password |
| `HOST` | `0.0.0.0` | Server host |
| `PORT` | `8000` | Server port |
| `DATABASE_URL` | `sqlite:///./data/hexanote.db` | SQLite database path |
| `WEAVIATE_URL` | `http://weaviate:8080` | Weaviate vector DB URL |
| `OLLAMA_URL` | `http://ollama:11434` | Ollama LLM server URL |
| `OLLAMA_EMBEDDING_MODEL` | `mxbai-embed-large:latest` | Embedding model |
| `OLLAMA_GENERATION_MODEL` | `llama3.2:1b` | Chat model |
| `ACCESS_TOKEN_EXPIRE_MINUTES` | `10080` (7 days) | Token expiration |

See [backend/.env.example](backend/.env.example) for full configuration options.

---

## TypeScript Types

For TypeScript/JavaScript clients, here are the main interface definitions from the Windows client:

```typescript
// From windows-client/src/services/api.ts

export interface Note {
    id: string
    title: string
    content: string
    tags: string[]
    version: number
    created_at?: string
    updated_at?: string
}

export interface SemanticSearchResult {
    note_id: string
    title: string
    content: string
    tags: string[]
    created_at?: string
    updated_at?: string
    relevance_score?: number
}

export interface ChatRequest {
    message: string
    session_id?: string
    limit?: number
    additional_context?: string
}

export interface ContextNote {
    note_id: string
    title: string
    content_preview: string
    relevance_score?: number
}

export interface ChatResponse {
    message: string
    session_id: string
    context_notes: ContextNote[]
    created_at: string
}
```

---

## Quick Start Example

Here's a complete example of authenticating and creating a note:

```bash
# 1. Get access token
curl -X POST http://localhost:8001/api/v1/token \
  -H "Content-Type: application/json" \
  -d '{"password": "hexanote"}'

# Response: {"access_token": "eyJ...", "token_type": "bearer", "expires_in": 604800}

# 2. Create a note (use the token from step 1)
curl -X POST http://localhost:8001/api/v1/notes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJ..." \
  -d '{
    "title": "My First Note",
    "content": "# Hello World\n\nThis is my first note!",
    "tags": ["welcome"]
  }'

# 3. Search notes
curl -X GET "http://localhost:8001/api/v1/notes/search/semantic?q=hello&limit=5" \
  -H "Authorization: Bearer eyJ..."
```

---

## OpenAPI/Swagger Documentation

The API also provides interactive documentation at:
- **Swagger UI:** http://localhost:8001/docs
- **ReDoc:** http://localhost:8001/redoc
- **OpenAPI JSON:** http://localhost:8001/openapi.json

---

## Need Help?

- Check the [main README](README.md) for setup instructions
- Review the Windows client code in [windows-client/src/services/api.ts](windows-client/src/services/api.ts) for working examples
- View backend implementation in [backend/routers/](backend/routers/)
