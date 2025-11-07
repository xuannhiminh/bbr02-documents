# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep class com.artifex.solib.** { *; }
-keep class com.artifex.mupdf.** { *; }
-keep class office.file.ui.** { *; }
-keep class kankan.wheel.widget.** { *; }
-keep class com.itextpdf.** { *; }
-keep class com.android.vending.billing.**
-keep class com.android.billingclient.api.**
# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn java.lang.invoke.StringConcatFactory
# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn com.box.androidsdk.content.auth.BoxAuthentication$BoxAuthenticationInfo
-dontwarn com.daimajia.easing.Glider
-dontwarn com.daimajia.easing.Skill
-dontwarn com.dropbox.core.e.a
-dontwarn com.facebook.infer.annotation.Nullsafe$Mode
-dontwarn com.facebook.infer.annotation.Nullsafe
-dontwarn com.microsoft.identity.client.c.d
-dontwarn com.microsoft.identity.client.i
-dontwarn com.microsoft.identity.client.j
-dontwarn com.microsoft.identity.client.q
-dontwarn javax.xml.crypto.XMLCryptoContext
-dontwarn javax.xml.crypto.dom.DOMCryptoContext
-dontwarn javax.xml.crypto.dom.DOMStructure
-dontwarn javax.xml.crypto.dsig.CanonicalizationMethod
-dontwarn javax.xml.crypto.dsig.DigestMethod
-dontwarn javax.xml.crypto.dsig.Reference
-dontwarn javax.xml.crypto.dsig.SignatureMethod
-dontwarn javax.xml.crypto.dsig.SignedInfo
-dontwarn javax.xml.crypto.dsig.Transform
-dontwarn javax.xml.crypto.dsig.XMLObject
-dontwarn javax.xml.crypto.dsig.XMLSignContext
-dontwarn javax.xml.crypto.dsig.XMLSignature
-dontwarn javax.xml.crypto.dsig.XMLSignatureFactory
-dontwarn javax.xml.crypto.dsig.dom.DOMSignContext
-dontwarn javax.xml.crypto.dsig.keyinfo.KeyInfo
-dontwarn javax.xml.crypto.dsig.keyinfo.KeyInfoFactory
-dontwarn javax.xml.crypto.dsig.keyinfo.KeyValue
-dontwarn javax.xml.crypto.dsig.keyinfo.X509Data
-dontwarn javax.xml.crypto.dsig.spec.C14NMethodParameterSpec
-dontwarn javax.xml.crypto.dsig.spec.DigestMethodParameterSpec
-dontwarn javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec
-dontwarn javax.xml.crypto.dsig.spec.TransformParameterSpec
-dontwarn javax.xml.crypto.dsig.spec.XPathFilter2ParameterSpec
-dontwarn javax.xml.crypto.dsig.spec.XPathType$Filter
-dontwarn javax.xml.crypto.dsig.spec.XPathType
-dontwarn office.equal.piss.OkHttp3Downloader
-dontwarn org.apache.jcp.xml.dsig.internal.dom.DOMKeyInfoFactory
-dontwarn org.apache.jcp.xml.dsig.internal.dom.DOMReference
-dontwarn org.apache.jcp.xml.dsig.internal.dom.DOMSignedInfo
-dontwarn org.apache.jcp.xml.dsig.internal.dom.DOMUtils
-dontwarn org.apache.jcp.xml.dsig.internal.dom.DOMXMLSignature
-dontwarn org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI
-dontwarn org.apache.xml.security.utils.Base64
-dontwarn org.spongycastle.cert.X509CertificateHolder
-dontwarn org.spongycastle.cert.jcajce.JcaX509CertificateConverter
-dontwarn org.spongycastle.cert.jcajce.JcaX509CertificateHolder
-dontwarn org.spongycastle.cert.ocsp.BasicOCSPResp
-dontwarn org.spongycastle.cert.ocsp.CertificateID
-dontwarn org.spongycastle.cert.ocsp.CertificateStatus
-dontwarn org.spongycastle.cert.ocsp.OCSPException
-dontwarn org.spongycastle.cert.ocsp.OCSPReq
-dontwarn org.spongycastle.cert.ocsp.OCSPReqBuilder
-dontwarn org.spongycastle.cert.ocsp.OCSPResp
-dontwarn org.spongycastle.cert.ocsp.SingleResp
-dontwarn org.spongycastle.cms.CMSEnvelopedData
-dontwarn org.spongycastle.cms.Recipient
-dontwarn org.spongycastle.cms.RecipientId
-dontwarn org.spongycastle.cms.RecipientInformation
-dontwarn org.spongycastle.cms.RecipientInformationStore
-dontwarn org.spongycastle.cms.SignerInformationVerifier
-dontwarn org.spongycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder
-dontwarn org.spongycastle.cms.jcajce.JceKeyTransEnvelopedRecipient
-dontwarn org.spongycastle.cms.jcajce.JceKeyTransRecipient
-dontwarn org.spongycastle.jcajce.provider.digest.GOST3411$Digest
-dontwarn org.spongycastle.jcajce.provider.digest.MD2$Digest
-dontwarn org.spongycastle.jcajce.provider.digest.MD5$Digest
-dontwarn org.spongycastle.jcajce.provider.digest.RIPEMD128$Digest
-dontwarn org.spongycastle.jcajce.provider.digest.RIPEMD160$Digest
-dontwarn org.spongycastle.jcajce.provider.digest.RIPEMD256$Digest
-dontwarn org.spongycastle.jcajce.provider.digest.SHA1$Digest
-dontwarn org.spongycastle.jcajce.provider.digest.SHA224$Digest
-dontwarn org.spongycastle.jcajce.provider.digest.SHA256$Digest
-dontwarn org.spongycastle.jcajce.provider.digest.SHA384$Digest
-dontwarn org.spongycastle.jcajce.provider.digest.SHA512$Digest
-dontwarn org.spongycastle.jce.X509Principal
-dontwarn org.spongycastle.jce.provider.BouncyCastleProvider
-dontwarn org.spongycastle.jce.provider.X509CertParser
-dontwarn org.spongycastle.ocsp.RevokedStatus
-dontwarn org.spongycastle.operator.ContentVerifierProvider
-dontwarn org.spongycastle.operator.DigestCalculator
-dontwarn org.spongycastle.operator.DigestCalculatorProvider
-dontwarn org.spongycastle.operator.OperatorCreationException
-dontwarn org.spongycastle.operator.bc.BcDigestCalculatorProvider
-dontwarn org.spongycastle.operator.jcajce.JcaContentVerifierProviderBuilder
-dontwarn org.spongycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder
-dontwarn org.spongycastle.tsp.TimeStampRequest
-dontwarn org.spongycastle.tsp.TimeStampRequestGenerator
-dontwarn org.spongycastle.tsp.TimeStampResponse
-dontwarn org.spongycastle.tsp.TimeStampToken
-dontwarn org.spongycastle.tsp.TimeStampTokenInfo