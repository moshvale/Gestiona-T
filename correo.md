Account credentials
Account details generated using Faker.js

NB! these credentials are shown only once. If you do not write these down then you have to create a new account.

Name	Clifford Shanahan
Username	clifford.shanahan55@ethereal.email – this account can not be used for inbound emails 
Password	KbJYYGccydKwa4rnUw
 

Nodemailer configuration
const transporter = nodemailer.createTransport({
    host: 'smtp.ethereal.email',
    port: 587,
    auth: {
        user: 'clifford.shanahan55@ethereal.email',
        pass: 'KbJYYGccydKwa4rnUw'
    }
});
PHPMailer configuration
$mail = new PHPMailer(true);
$mail->isSMTP();
$mail->Host = 'smtp.ethereal.email';
$mail->SMTPAuth = true;
$mail->Username = 'clifford.shanahan55@ethereal.email';
$mail->Password = 'KbJYYGccydKwa4rnUw';
$mail->SMTPSecure = 'tls';
$mail->Port = 587;
SwiftMailer configuration
$transport = (new Swift_SmtpTransport('smtp.ethereal.email', 587, 'tls'))
  ->setUsername('clifford.shanahan55@ethereal.email')
  ->setPassword('KbJYYGccydKwa4rnUw');

SMTP configuration
Host	smtp.ethereal.email
Port	587
Security	STARTTLS
Username	clifford.shanahan55@ethereal.email
Password	KbJYYGccydKwa4rnUw

IMAP configuration
Host	imap.ethereal.email
Port	993
Security	TLS
Username	clifford.shanahan55@ethereal.email
Password	KbJYYGccydKwa4rnUw

POP3 configuration
Host	pop3.ethereal.email
Port	995
Security	TLS
Username	clifford.shanahan55@ethereal.email
Password	KbJYYGccydKwa4rnUw