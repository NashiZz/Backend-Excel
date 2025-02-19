# Backend-Excel

## คำอธิบายโครงการ
โครงการ **Backend-Excel** เป็นระบบ Backend ที่พัฒนาเพื่อจัดการข้อมูลจากไฟล์ Excel โดยใช้ API สำหรับอัปโหลดและประมวลผลข้อมูล รองรับการอ่านและแปลงข้อมูลให้อยู่ในรูปแบบที่สามารถนำไปใช้ต่อได้

## เทคโนโลยีที่ใช้
- **Spring Boot** - ใช้เป็น Backend หลักของระบบ
- **Apache POI** - ใช้สำหรับอ่านและแปลงข้อมูลจากไฟล์ Excel
- **Firebase Storage** - ใช้สำหรับเก็บไฟล์ Template และไฟล์ที่อัปโหลด

## การติดตั้งและใช้งาน
### Clone Repository
```sh
git clone https://github.com/NashiZz/Backend-Excel.git
cd Backend-Excel
```

## โครงสร้างโฟลเดอร์
```
Backend-Excel/
├── config/            # จัดการการตั้งค่า Config ต่างๆ
├── controller/        # จัดการการทำงานของ API
├── dto/               # กำหนดโครงสร้างของข้อมูลที่ใช้ใน API
├── service/           # ส่วนที่จัดการทำงานรับการทำงานต่อจาก Controller
├── validate/          # ตรวจสอบความถูกต้องของข้อมูลที่รับเข้ามาใน API
```

## ผู้พัฒนา
- **Nashi**
- **Depttt**

