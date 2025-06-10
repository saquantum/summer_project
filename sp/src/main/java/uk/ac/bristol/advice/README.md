This package stores all AOP components.

## LogAdvice

Keep a log when any method in the Service layer is invoked.

#### public void pt()

​    The formal cut point function for the AOP.

#### public void log(JoinPoint jp)

​    Triggered after the cut point. The parameter `jp` is the method being dynamically proxied. This method tries to visit the `logs/app.log` file and write to it. 