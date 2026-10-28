# MoonAsk Store — Android App Store & APK Distribution Platform

Build MoonAsk Store (`com.moonask.store`), a full-featured, mobile-first Android application distribution platform and app store with native APK package installer integration, Gemini AI assistant, Super Admin management, Developer Portal, and an app-centric short-video Reels feed.

### User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural decisions were confirmed through user input:
> - **Developer Model**: Open developer registration with mandatory Super Admin approval for pending app submissions and releases.
> - **Monetization & Pricing**: Free APK downloads with Super Admin controlled featured app badges and slots.
> - **Initial Scope**: Complete Android App Store, Developer Portal, Super Admin Console, App Reels feed, and Gemini AI assistant & security scanner.
> - **Sharing System**: Gmail and Android native share sheets distributing the app listing URL and direct APK download links with clear installation guidance.
> - **Authentication Options**: Email/Password login, Google Sign-In via Credential Manager, and Phone OTP verification.
> - **Super Admin Access**: Strictly gated for `uddinsknijam102@gmail.com` (password: `7996098760@3`) with server-enforced role verification and audit logging.
> - **Native Package Installer**: Android system `PackageInstaller` / `FileProvider` download and installation pipeline with SHA-256 integrity checks, unknown sources permission handling, and installed package state detection.
> - **Gemini AI Integration**: Gemini 3.8 Flash integrated via Firebase AI Logic for conversational app discovery, natural-language search, reviews summarization, and APK manifest permission safety audits.

---

### 1. Overview & Core Concept

- **What It Does**: MoonAsk Store is a standalone Android app distribution ecosystem that allows Android users to discover, search, review, and install APKs using the official Android system package installer. Developers can create listings and submit APK releases for review. The Super Admin reviews releases, manages categories, inspects security permissions, and approves publications. Users can also watch app showcase Reels and ask Gemini AI for recommendations and APK safety analysis.
- **Target Audience / Persona**:
  - *Android Users*: Looking for an open, curated, trustworthy alternative app store with free downloads, community reviews, and Reels demonstrations.
  - *Developers*: Individual and indie developers seeking an accessible portal to publish APKs and updates without complex fee hurdles.
  - *Super Admin & Moderators*: Overseeing platform safety, approving releases, managing featured apps, and resolving abuse reports.
- **Key Value**: A complete, functional Android app store experience with real package download and install capabilities, verified security checks, creator Reels, and Gemini AI search.

---

### 2. User Experience & Visual Design

#### Visual Identity & Design System (Matching Provided Attachment)
- **Identity & Tone**: Tech-forward, trustworthy, polished, and modern. Original MoonAsk branding utilizing deep celestial midnight navy (`#0B0F19`), rich electric cobalt (`#1A56DB`), vibrant cyan/teal accents (`#06B6D4`), and crisp white/slate surfaces.
- **Typography**: Clean, readable sans-serif pairing with bold display headers, clear subheadings, and high-legibility body fonts with full accessibility scaling (`sp`).
- **Attachment Alignment**:
  - **Profile Dashboard Grid**: Matches page 1 of the attached designs with stat counters (Apps, Users, Releases, Reviews), 12-item quick access grid (Users, Developers, Apps, Reports, APK Files, Releases, Categories, Settings, Reviews, Transactions, Support Chats, Admin), and prominent "Publish your first app" button.
  - **Terms & Privacy Screen**: Matches page 2 with segmented tab control ("Terms of Service" and "Privacy Policy") and structured legal terms.
  - **Bottom Navigation**: 5-point navigation featuring Home, Apps, Center Floating Action Button (+), Reels, and Profile.

#### Key User Screens & Flows
1. **Home Screen**:
   - Top app bar with MoonAsk Store branding, Gemini AI Search bar, notifications icon, and user avatar.
   - Hero banner featuring Editor's Choice and top trending releases.
   - Horizontal scrolling rows: "Featured Apps", "Popular Utilities", "Recently Published", and "Top Games".
   - App Card: High-res icon, title, developer name, star rating with review count, category badge, and instant "Install" / "Open" action.
2. **Search & Discovery**:
   - Real-time keyword filter, search history pills, category chips, and filter drawer (Sort by rating, date, size).
   - Gemini AI Natural Search bar: "Find me a privacy-focused notes app with markdown support".
3. **App Details Screen**:
   - Header with icon, verified developer badge, version tag, download size, and SHA-256 checksum status.
   - Download/Install state machine: *Install → Preparing → Downloading (Progress % & MBs) → Verifying Checksum → Launching System Installer → Installed / Open*.
   - Screenshot gallery carousel with full-screen zoom preview.
   - Gemini AI Summary: 1-paragraph summary of user reviews and pros/cons.
   - Declared Android permissions table with safety level tags (Safe, Normal, Dangerous).
   - User reviews list, rating distribution bar graph, and "Write a Review" dialog.
   - Sharing menu: "Share via Gmail", "Copy Listing Link", "Copy APK Download Link".
4. **Reels Feed (Short-Video App Showcase)**:
   - Full-height vertical video feed showcasing app demos, gameplay snippets, and tutorials.
   - Overlay controls: Creator avatar, like button with counter, comment drawer, share button, and direct "View App in Store" button linked to the featured listing.
5. **Developer Portal**:
   - My Apps dashboard with status pills: *Draft*, *Pending Review*, *Approved*, *Published*, *Changes Requested*, *Rejected*.
   - "Submit New App" multi-step wizard: App Metadata, Icon/Screenshots, APK Upload with SHA-256 calculation, and Permission disclosure.
6. **Super Admin Console**:
   - Gated specifically for `uddinsknijam102@gmail.com`.
   - App Review Queue: Displays pending releases, version codes, APK size, SHA-256, permissions analysis, and review actions (*Approve & Publish*, *Request Changes*, *Reject* with reason).
   - Platform Analytics: Registered users, developer count, published apps, download velocity.
   - Moderation: Reports on suspicious apps, reviews, or broken download links.
7. **Gemini AI Store Assistant**:
   - Dedicated interactive AI sheet: Personalized app recommendations, APK permission safety checks, and release notes comparisons.

---

### 3. Key Product Decisions & Trade-Offs

| Decision | Chosen Approach | Rationale & Trade-offs |
|---|---|---|
| **Android Installation Engine** | Native Android `PackageInstaller` / `FileProvider` intent with `ACTION_INSTALL_PACKAGE` / `ACTION_VIEW` | Browser-based downloads cannot trigger native system package installation prompts. Native intents allow clean system UI prompts while respecting user consent and unknown sources settings. |
| **Backend & Persistence** | Firebase Firestore + Firebase Auth with Local Room Cache | Provides real-time synchronization, role-based authorization, and reliable cloud storage, while Room ensures fast offline caching and installed package persistence. |
| **Authentication Flow** | Multi-Option Auth: Email/Password, Google Sign-In via Credential Manager, and Phone OTP | Fulfills all user requirements while maintaining strict role checks on the Super Admin account (`uddinsknijam102@gmail.com`). |
| **AI Architecture** | Firebase AI Logic (`gemini-3.8-flash`) | Zero-trust App Check attestation without exposing private API keys in client builds; provides high-speed streaming for store assistance and structured JSON output for permission scans. |
| **Sharing Mechanism** | Dual Gmail & Native Intent Sharing | Generates a formatted message containing both the MoonAsk Store listing URL and the authorized direct APK download link, accompanied by friendly Android installation instructions. |

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                          MoonAsk Store App                             │
│                     (Kotlin + Jetpack Compose)                         │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
    ┌───────────────────────────────┼──────────────────────────────┐
    │                               │                              │
┌───▼─────────────────────┐ ┌───────▼────────────────────┐ ┌───────▼────────────────────┐
│   User App Store & UI   │ │   Developer Portal & Admin │ │  Gemini AI Store Assistant │
│ - Home & Category Feeds │ │ - App Submission Form      │ │ - Conversational Search    │
│ - Search & Filters      │ │ - Release State Machine    │ │ - Review Summaries         │
│ - App Details & Reviews │ │ - Admin Approval Queue     │ │ - Permissions Inspector    │
│ - Reels Short-Video Feed│ │ - Audit Logging & Reports  │ │ - Firebase AI Logic SDK    │
└───┬─────────────────────┘ └───────┬────────────────────┘ └───────┬────────────────────┘
    │                               │                              │
    └───────────────────────────────┼──────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│                    Core Platform Engines & Services                    │
├────────────────────────────────┬───────────────────────────────────────┤
│    Package Installer Engine    │          Security & Auth Layer        │
│ - Android DownloadManager/Http │ - Jetpack Credential Manager (Google) │
│ - SHA-256 Checksum Validator   │ - Email & Phone OTP Auth              │
│ - FileProvider APK Content URI │ - Super Admin Role Enforcement        │
│ - PackageInstaller / View Intent│ - Zero-trust Firestore Security Rules │
├────────────────────────────────┴───────────────────────────────────────┤
│                   Data Persistence & Synchronization                   │
│ - Cloud Firestore (Apps, Releases, Reviews, Users, Reels, Reports)     │
│ - Room Database (Local Installed Apps Cache, Offline Favorites)        │
└────────────────────────────────────────────────────────────────────────┘
```

#### Core Data Entities
1. **User (`users/{uid}`)**: `uid`, `email`, `displayName`, `role` (`guest`, `user`, `developer`, `admin`), `photoUrl`, `createdAt`.
2. **App Listing (`apps/{appId}`)**: `appId`, `ownerUid`, `name`, `packageName`, `categoryId`, `shortDescription`, `fullDescription`, `iconUrl`, `screenshots`, `ratingAverage`, `ratingCount`, `downloadCount`, `latestPublishedReleaseId`, `isFeatured`, `createdAt`.
3. **Release (`apps/{appId}/releases/{releaseId}`)**: `releaseId`, `appId`, `versionName`, `versionCode`, `apkUrl`, `sha256`, `fileSizeBytes`, `minSdk`, `targetSdk`, `permissions`, `whatsNew`, `status` (`DRAFT`, `PENDING_REVIEW`, `CHANGES_REQUESTED`, `APPROVED`, `PUBLISHED`, `REJECTED`), `submittedAt`, `reviewedAt`.
4. **Review (`apps/{appId}/reviews/{reviewId}`)**: `reviewId`, `appId`, `userId`, `authorName`, `rating`, `comment`, `createdAt`.
5. **Reel (`reels/{reelId}`)**: `reelId`, `creatorUid`, `creatorName`, `title`, `description`, `videoUrl`, `thumbnailUrl`, `linkedAppId`, `likesCount`, `createdAt`.
6. **InstalledApp (Room Cache)**: `packageName`, `appId`, `versionName`, `versionCode`, `installDate`, `lastCheckedDate`.

---

### 5. Implementation Stages & Build Steps

- **Stage 1: Environment & Branding Setup**
  - Update `applicationId` to `com.moonask.store` and app name to "MoonAsk Store".
  - Configure Material 3 Theme with MoonAsk celestial palette, typography, and generate custom adaptive launcher icon.
  - Set up `AndroidManifest.xml` permissions: `INTERNET`, `REQUEST_INSTALL_PACKAGES`, `POST_NOTIFICATIONS`, `ACCESS_NETWORK_STATE`, and configure `FileProvider`.
- **Stage 2: Authentication & Role-Based Access Control**
  - Implement Auth repository supporting Email/Password, Google Sign-In via Credential Manager, and Phone OTP verification.
  - Implement Super Admin role verification locking admin actions to `uddinsknijam102@gmail.com`.
- **Stage 3: Cloud Firestore & Room Data Layer**
  - Implement repository classes for Apps, Releases, Reviews, Reels, and local InstalledApps.
  - Define Firestore security rules enforcing role boundaries and publication status.
- **Stage 4: App Store Presentation & Navigation**
  - Scaffold 5-point navigation matching user attachment: Home, Apps, (+ FAB), Reels, and Profile dashboard.
  - Build Home, Category browsing, Search with filters, and detailed App view.
  - Build Terms & Privacy screen matching attachment Page 2.
- **Stage 5: Native Package Installer & Download Manager**
  - Build robust Android download pipeline saving APK to app cache.
  - Implement SHA-256 checksum verification.
  - Launch package installer via `FileProvider` with user permission fallback for `REQUEST_INSTALL_PACKAGES`.
  - Implement Gmail and native share sheets with listing link, direct APK link, and instructions.
- **Stage 6: Developer Portal & Super Admin Console**
  - Developer portal: App creation wizard, APK upload, versioning, release history.
  - Super Admin console: Pending queue, approve/publish/reject flow, category manager, and featured app controls.
- **Stage 7: Reels Feed & Gemini AI Assistant**
  - Vertical short-video feed with like/comment interactions and linked app navigation.
  - Gemini AI assistant powered by Firebase AI Logic for natural app search, review summarization, and permission security audits.
- **Stage 8: Compilation, Testing & Verification**
  - Run `compile_applet` to ensure error-free compilation and verify all Jetpack Compose UI screens.
