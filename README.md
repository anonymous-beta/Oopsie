# Oopsie
A view-once media recovery tool for your own WhatsApp, on your own device.

Made by **Anonymous-beta**.

## What it actually does
Oopsie walks the WhatsApp data tree on your phone, sniffs each file by magic bytes,
and copies readable media out to `Pictures/Oopsie/`. It targets the private, status
and cache regions where view-once media lands before WhatsApp purges it.

It does **not** modify WhatsApp, does **not** use a modded client, and does **not**
touch the network. Stock WhatsApp, untouched. That is the whole point — no ban risk
from the app itself.

## The honest caveat
Some WhatsApp builds write view-once files as AES-encrypted blobs with `.enc` or
no extension. Oopsie will list those as `LOCKED` in the diagnostics panel but cannot
decrypt them — the key lives inside WhatsApp's process. Run a scan, open
**diagnostics**, and look at the *by kind* line. If you see `image=N` you're golden.
If everything reads `locked`, your build encrypts and this approach stops there.
