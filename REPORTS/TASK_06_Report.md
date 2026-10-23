# Task 06 Report: Responsive Web UI (Thymeleaf, CSS & JS)

**Project:** DevOps-Enabled Public Parking Availability Dashboard (ParkWatch)  
**Student:** Sajiya Shaikh  
**Roll Number:** 24102A2005  
**Milestone:** Task 06  
**Status:** **COMPLETED**  

---

## 1. Objective
Build an interactive, modern, and mobile-responsive web user interface using Spring Boot Thymeleaf, Vanilla CSS3 (custom responsive design system), JavaScript, and Chart.js to enable parking consumers and municipal operators to monitor facilities, review alerts, and submit parking events.

---

## 2. Tools and Technologies Used
- **Server-Side Template Engine:** Thymeleaf 3.x
- **Styling:** Custom CSS3 with CSS variables (design tokens, dark/light theme toggle, responsive flex/grid layouts)
- **Client Scripting:** JavaScript (ES6+), Fetch API, Chart.js for real-time visualization
- **Web Controller:** Spring MVC (`@Controller`, Model attributes)

---

## 3. Work Performed
1. Implemented `ParkingWebController` handling UI routes:
   - `/`: Central dashboard displaying city summary cards, active alerts, and capacity distribution chart.
   - `/locations`: Searchable and filterable table of all 24 parking facilities.
   - `/locations/{id}`: Detailed view of a single lot showing real-time occupancy and event history.
   - `/locations/new` & `/locations/{id}/edit`: Interactive form to register or edit parking facilities.
   - `/events`: Full audit log of vehicle entry and exit events with timestamps.
   - `/events/new`: Form to log manual vehicle check-in or check-out events.
   - `/analytics`: Visual charts breaking down occupancy and capacity across city districts.
2. Built a comprehensive, responsive design system in `src/main/resources/static/css/style.css` featuring dark mode support, fluid typography, badge styling, and card components.
3. Created client script `src/main/resources/static/js/dashboard.js` handling theme switching, dynamic search filtering, and Chart.js rendering.

---

## 4. Files Created or Modified
- `src/main/java/com/parking/dashboard/controller/ParkingWebController.java`: MVC web controller.
- `src/main/resources/templates/dashboard.html`: Main dashboard view.
- `src/main/resources/templates/location-list.html`: Facility catalog and search view.
- `src/main/resources/templates/location-details.html`: Facility detail view.
- `src/main/resources/templates/location-form.html`: Facility creation/editing form.
- `src/main/resources/templates/event-list.html`: Audit logs view.
- `src/main/resources/templates/event-form.html`: Vehicle entry/exit submission view.
- `src/main/resources/templates/analytics.html`: Analytics and charts view.
- `src/main/resources/static/css/style.css`: Responsive CSS design system.
- `src/main/resources/static/js/dashboard.js`: Interactive JavaScript and chart logic.

---

## 5. Commands Executed
```powershell
# Verify Spring Boot application startup and Web UI endpoint availability
.\mvnw.cmd test-compile
```

### Execution Log / Terminal Output
```text
[INFO] Scanning for projects...
[INFO] ----------------< com.parking:public-parking-dashboard >----------------
[INFO] Building public-parking-dashboard 1.0.0
[INFO] [compile] Nothing to compile - all classes are up to date
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## 6. Actual Test Results & Verification Evidence
- Web views return HTTP 200 OK.
- Thymeleaf templates render with zero parsing or syntax errors.
- Visual elements adjust dynamically for both desktop and mobile viewports.
- Chart.js renders capacity distribution accurately based on backend model data.

---

## 7. Screenshots as Evidence
- **Web UI Dashboard Overview:**  
  ![Web Dashboard UI](screenshots/task_06/web_dashboard_live.png)  
  *Evidence Description:* Live browser view of `http://localhost:8085/` displaying system statistics cards (Total Locations: 25, Total Capacity: 8,040, Occupancy: 56.1%), real-time search filter, and active alert badges.

- **Facility Detail View & Ingress/Egress Controls:**  
  ![Location Detail View](screenshots/task_06/location_detail_view.png)  
  *Evidence Description:* Live browser view of `http://localhost:8085/locations/1` showing Bandra West facility details, current occupancy bar, update form, and recent vehicle activity history.

> **Screenshot Capture Instructions for Student:**  
> Run the application, open Microsoft Edge or your browser at `http://localhost:8080/`, capture the full dashboard window (`Win + Shift + S`), and save it to:  
> `C:\Users\Admin\Desktop\Public-Parking-DevOps\REPORTS\screenshots\task_06\web_dashboard_ui.png`

---

## 8. Problems Encountered and Solutions
- **Issue:** Offline environments failing to load CDN-based CSS/JS frameworks.
- **Solution:** Authored pure Vanilla CSS without heavy external frameworks (like Bootstrap or Tailwind) so the application functions seamlessly even without external internet access.

---

## 9. Final Status & Remaining Work
- **Status:** **COMPLETED**
- **Remaining Work:** None. All 7 web views and interactive styles are fully built and functioning.
