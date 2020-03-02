# Email Service
Email sending service

## Publicly hosted email images

These are in a London S3 bucket named dvla-ved-communications-assets. 

 * The bucket is public for all assets. 
 * Access is logged to a separate bucket: dvla-ved-communications-assets-access-logs.
 * Assets are versioned, to help in case of human-error. 

### Policy guidance for using the bucket.

By changing existing assets, you may affect the rendering of emails that are in-flight or 
delivered and foldered. 

 * In general do not delete assets that ever reached production or beta environment -- they may be 
   referred to from emails that will be retained for a long period.
 * In general do not rename an asset. 
 * In general do not change an asset. 
 * Instead of renaming or changing an asset, fetch the asset, make the change and add a new instance of the 
   changed/renamed asset
 * Use lightweight paths for easy maintenance. Examples::
 
    <bucket>/ved/email/image/crown.png
    
    <bucket>/ved/email/pdf/privacy-policy.pdf
    
 
The initial images are:

 * https://s3.eu-west-2.amazonaws.com/dvla-ved-communications-assets/ved/email/image/gov.uk_logotype_crown.png

 * https://s3.eu-west-2.amazonaws.com/dvla-ved-communications-assets/ved/email/image/govuk-crest_x2.png

Or the shorter and more 'branded' domain prefix URLs can be used:

 * https://dvla-ved-communications-assets.s3.amazonaws.com/ved/email/image/gov.uk_logotype_crown.png

 * https://dvla-ved-communications-assets.s3.amazonaws.com/ved/email/image/govuk-crest_x2.png

