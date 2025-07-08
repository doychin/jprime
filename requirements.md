# **Web Application Specification**

## **Project Overview**
The web application will serve as a platform to manage conference-related activities, including sponsorship requests, ticket purchases, CFP submissions, contract signing processes, and document issuance (invoices or proforma invoices). The application ensures a secure and efficient workflow for attendees, sponsors, administrators, and speakers.

---

## **Key Features and Functionalities**

### **1. User Registration and Authentication**
- **Registration Options:**
  - Manual registration with a new account.
  - Registration via third-party authentication providers (e.g., GitHub, Facebook, LinkedIn, Office365, Google).
- **Mandatory Additional Information:**
  - After registration using a third-party provider, users will need to fill in customizable additional information fields before continuing to use the site.
- **Authentication Service:**
  - Authentication will be handled via Keycloak to provide secure token-based user authentication.

---

### **2. CFP (Call for Papers) Process**
- **Submission of Talks and Workshops:**
  - A registered user can submit a talk or workshop for an event.
  - Each submission is stored and linked to the user account for tracking.
- **Review Process:**
  - Submissions undergo a review process by administrators or event reviewers.
- **Speaker Notification:**
  - Speakers with approved submissions will receive an email notification containing:
    - A link to confirm their participation in the event.
- **Participation Confirmation:**
  - Once confirmed, speakers are required to:
    - Upload a profile picture.
    - Provide or update additional information, such as social media links (e.g., X/Twitter, BlueSky).
    - If such data already exists in their profile, they can confirm or update it.
- **Admin Management:**
  - Admins can track and manage submissions, including:
    - Approving or rejecting talks.
    - Sending confirmation links to speakers with approved submissions.
  - The system will maintain a record of confirmed submissions and speaker profiles.

---

### **3. Sponsorship Management**
- **Submission of Sponsorship Requests:**
  - Sponsors can submit sponsorship requests for specific conference events.
- **Contract Signing Workflow:**
  - If the requested sponsorship package is available:
    1. A preliminary contract for the package is sent to the sponsor.
    2. Upon mutual confirmation, the final contract is signed electronically.
    3. Once the contract is confirmed and signed:
       - The admin determines whether to issue an invoice immediately or a proforma invoice first, based on the sponsor's payment method.
- **Package Management:**
  - Admins can create, edit, and delete sponsorship packages with details such as tier, benefits, and cost.

---

### **4. Ticketing System**
- **Ticket Purchase:**
  - Users select and purchase event tickets from an available list.
  - Payment methods include credit card, PayPal, or bank transfer.
- **Admin Control for Documents:**
  - For payments made via bank transfers, the admin can choose to issue:
    - **Invoice**: Directly issued upon ticket purchase.
    - **Proforma Invoice**: Issued prior to payment, and the actual invoice is generated only after the bank transfer payment is confirmed.
- **Real-Time Seat Availability:**
  - Tickets remain available for events until sold out or the event date is reached.

---

### **5. Invoice and Proforma Invoice Management**
- **Proforma Invoice Workflow:**
  - For both ticket purchases and sponsorship packages paid by bank transfers:
    - The admin issues a proforma invoice first, outlining the expected payment.
    - Once the payment is confirmed:
      - The actual invoice is generated, replacing the proforma invoice.
      - The final invoice reflects all details of the confirmed payment.
- **Invoice Generation:**
  - Immediate invoices are issued for credit card or PayPal payments.
- **Admin Options:**
  - Admins can manage the status of proforma invoices and confirm payments to trigger invoice generation.

---

### **6. Workflow for Sponsors**
- **Login/Register:**
  - Sponsors register using an account or third-party authentication provider.
- **Submit Sponsorship Request:**
  - Sponsors submit a request for an available package.
- **Contract Signing:**
  - Review and finalize the preliminary contract.
  - Sign the final contract electronically.
- **Payment and Document Handling:**
  - For bank transfers:
    - Admin issues proforma invoices initially.
    - Final invoices issued automatically upon confirmed payment.

---

### **7. Workflow for Attendees**
- **Login/Register:**
  - Attendees register with an account or third-party authentication provider.
- **Purchase Tickets:**
  - Select tickets for a specific event and proceed to payment.
  - For payments via bank transfer:
    - Proforma invoices are issued initially, with invoices generated after payment confirmation.

---

### **8. Reporting and Document Management**
- **Admin Dashboard:**
  - Overview of sponsorship requests, ticket sales, CFP submissions, and pending proforma invoices.
  - Detailed tracking of pending vs paid proforma invoices.
- **Invoice and Payment Status:**
  - Summary of all invoices and proforma invoices, with payment statuses (Paid, Pending, Confirmed, Overdue).
- **CFP Submission Metrics:**
  - Reports on the status of submissions (e.g., Approved, Rejected, Pending).
  - Data visualization options for speaker participation and submission trends.

---

## **Technical Specifications**

### **1. Technology Stack**
- **Frontend:** React.js or Angular for an interactive user interface.
- **Backend:** Java for all backend operations.
- **Database:** MySQL for structured data management.
- **Authentication Service:** Keycloak for user authentication and third-party provider integration.
- **Document Storage and Generation:**
  - Templates for both invoices and proforma invoices for easy customization.
  - Generated documents will be stored securely and accessible in the user’s dashboard.

---

### **2. Deployment and Hosting**
- **Hosting Platform:** Cloud solutions such as AWS/GCP/Azure.
- **Security Measures:**
  - Sensitive document uploads and user data handled over HTTPS.
  - Banking details and payment records encrypted.

---

### **Detailed Workflow for Proforma and Invoice Issuance**

1. **For Bank Transfers:**
    - **Step 1:** User selects sponsorship package or event ticket.
    - **Step 2:** The system identifies the payment method as "Bank Transfer."
    - **Step 3:** Admin issues a proforma invoice upon request submission.
    - **Step 4:** Payment confirmation detected in the system (via manual admin approval or automatic flag from a banking system).
    - **Step 5:** The final invoice is issued, replacing the proforma invoice in the user’s dashboard.

2. **For Direct Payments (Credit Card, PayPal):**
    - An invoice is issued immediately after payment completion.

---

### **Future Enhancements**
- Enable automated reminders for speakers about pending confirmations.
- Automate bank payment confirmation using third-party banking APIs.
- Add custom reports tailored to CFP data, such as speaker engagement metrics.

---