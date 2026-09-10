import zipfile,struct,sys
bad=[]; relro=[]; count=0
with zipfile.ZipFile(sys.argv[1]) as z:
 for name in z.namelist():
  if not name.endswith('.so') or not any(abi in name for abi in ['arm64-v8a','x86_64']): continue
  b=z.read(name); count+=1
  order='<' if b[5]==1 else '>'
  phoff=struct.unpack_from(order+'Q',b,32)[0]; entsize,num=struct.unpack_from(order+'HH',b,54)
  for i in range(num):
   p=struct.unpack_from(order+'IIQQQQQQ',b,phoff+i*entsize)
   if p[0]==1 and p[7]<16384: bad.append((name,'LOAD',p[7]))
   if p[0]==0x6474e552 and (p[3]+p[6])%16384: relro.append((name,'RELRO',hex(p[3]+p[6])))
print('64-bit libraries checked:',count,'LOAD alignment failures:',bad)
print('RELRO boundaries to review on a 16 KB device:', relro)
sys.exit(bool(bad))
