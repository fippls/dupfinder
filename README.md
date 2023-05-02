# DupFinder - File duplication finder
Java tool that uses MD5 sums to find duplicated files no matter what the actual file name is.

Uses a multi-step approach that is much quicker than just blindly running MD5 checks on the entire file system.

# Algorithm
1. Sort all files by file size and keep only those that are non-unique (if files don't have the same size, they can't be duplicates) - usually reduces the number of files to check by over 80%
2. Do a quick MD5 scan of the first 20k bytes of all files that are potential duplicates to see if they match at all, usually gives another 30-50% reduction in the number of files to check
3. Final step: do a full MD5 scan of all remaining files to determine which really *are* duplicated 

# Future plans
- Various methods to control automatic suggestions on which files to delete, for example if one of the duplicated files has "_copy of_" in its name
- More control over settings such as thread pool size, read buffers, etc.
- A GUI on top of this would be extremely useful but I've already attempted that and failed, UI work is not my strong suite

# How to use
Until there's a released version, build the entire project and run the "DupChecker" main class and supply path parameters to all places where you want to look for potentially duplicated files:
```
   DupFinder /home/user/Documents /mnt/externaldrive/documents
   DupFilder "c:\Users\aUser\Documents and Settings" u:\documents 
```